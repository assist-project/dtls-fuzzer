package de.rub.nds.modelfuzzer.learn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.SUL;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SymbolCounterSUL;
import de.rub.nds.modelfuzzer.config.ModelBasedTesterConfig;
import de.rub.nds.modelfuzzer.sut.NonDeterminismRetryingSUL;
import de.rub.nds.modelfuzzer.sut.ProcessHandler;
import de.rub.nds.modelfuzzer.sut.SulProcessWrapper;
import de.rub.nds.modelfuzzer.sut.TlsSUL;
import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Taken/adapted from StateVulnFinder tool.
 */
public class Extractor {
private static final Logger LOG = Logger.getLogger(Extractor.class
		.getName());
private final ModelBasedTesterConfig finderConfig;
private final Alphabet<TlsInput> alphabet;
public static final String LEARNED_MODEL_FILENAME = "learnedModel.dot";
public static final String STATISTICS_FILENAME = "statistics.txt";


public Extractor(ModelBasedTesterConfig finderConfig, Alphabet<TlsInput> alphabet) {
	this.finderConfig = finderConfig;
	this.alphabet = alphabet;
}

public ExtractorResult extractStateMachine() {
	ExtractorResult extractorResult = new ExtractorResult();

	// setting up our output directory
	File folder = new File(finderConfig.getOutput());
	folder.mkdirs();

	// setting up SUL/T (System Under Learning/Test)
	SUL<TlsInput, TlsOutput> tlsSystemUnderTest = new TlsSUL(
			finderConfig.getSulDelegate());
	
	if (finderConfig.getSulDelegate().getCommand() != null) {
		tlsSystemUnderTest = new SulProcessWrapper<TlsInput, TlsOutput>(
				tlsSystemUnderTest, new ProcessHandler(
						finderConfig.getSulDelegate()));
	}
	
	// we use a wrapper to check for non-determinism, we could use its
	// observation tree as cache
	try {
		tlsSystemUnderTest = new NonDeterminismRetryingSUL<TlsInput, TlsOutput>(
				tlsSystemUnderTest, 5, new FileWriter(new File(folder,
						"nondet.log")));
	} catch (IOException e) {
		e.printStackTrace();
	}

	SymbolCounterSUL<TlsInput, TlsOutput> symbolCounterSul = new SymbolCounterSUL<>(
			"symbol counter", tlsSystemUnderTest);
	ResetCounterSUL<TlsInput, TlsOutput> resetCounterSul = new ResetCounterSUL<>(
			"reset counter", symbolCounterSul);

	StatisticsTracker tracker = new StatisticsTracker(
			symbolCounterSul.getStatisticalData(),
			resetCounterSul.getStatisticalData());
	tlsSystemUnderTest = resetCounterSul;

	// we are adding a cache so that executions of same inputs aren't
	// repeated
	CachingSULOracle<TlsInput, TlsOutput> sulOracle = new CachingSULOracle<TlsInput, TlsOutput>(
			tlsSystemUnderTest);

	// setting up membership and equivalence oracles
	MealyLearner<TlsInput,TlsOutput> algorithm = 
			LearnerFactory.loadLearner(finderConfig.getLearningConfig(), sulOracle, alphabet);
	EquivalenceOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, Word<TlsOutput>> equivalenceAlgorithm =
			LearnerFactory.loadTester(finderConfig.getLearningConfig(), tlsSystemUnderTest, sulOracle);

	// running learning and collecting important statistics
	MealyMachine<?, TlsInput, ?, TlsOutput> hypothesis = null;
	DefaultQuery<TlsInput, Word<TlsOutput>> counterExample = null;
	int rounds = 0;

	algorithm.startLearning();
	tracker.startLearning(finderConfig, alphabet);
	do {
		hypothesis = algorithm.getHypothesisModel();
		StateMachine stateMachine = new StateMachine(hypothesis, alphabet);
		extractorResult.addHypothesis(stateMachine);
		// it is useful to print intermediate hypothesis as learning is
		// running
		serializeHypothesis(stateMachine, folder, "hyp" + (rounds + 1)
					+ ".dot", false);

		tracker.newHypothesis(stateMachine);
		counterExample = equivalenceAlgorithm.findCounterExample(
				hypothesis, alphabet);
		if (counterExample != null) {
			LOG.warning("Counterexample: " + counterExample.toString());
			tracker.newCounterExample(counterExample);
			algorithm.refineHypothesis(counterExample);
		}
		rounds++;
	} while (counterExample != null);

	// building results:
	StateMachine stateMachine = new StateMachine(hypothesis, alphabet);
	tracker.finishedLearning(stateMachine);
	Statistics statistics = tracker.generateStatistics();
	serializeHypothesis(stateMachine, folder, LEARNED_MODEL_FILENAME, true);

	LOG.log(Level.INFO, "Finished Learning");
	LOG.log(Level.INFO, "Number of Rounds:" + rounds);
	LOG.log(Level.INFO, statistics.toString());

	extractorResult.setLearnedModel(stateMachine);
	extractorResult.setStatistics(statistics);
	extractorResult.setLearnedModelFile(new File(folder, LEARNED_MODEL_FILENAME));

	return extractorResult;
}

private void serializeHypothesis(StateMachine hypothesis, File folder,
		String name, boolean genPdf) {
	File graphFile = new File(folder, name);
	hypothesis.export(graphFile, genPdf);
}

public static class ExtractorResult {

	public StateMachine learnedModel;
	public Statistics statistics;
	public final List<StateMachine> hypotheses;
	public File learnedModelFile;

	public File getLearnedModelFile() {
		return learnedModelFile;
	}

	public void setLearnedModelFile(File learnedModelFile) {
		this.learnedModelFile = learnedModelFile;
	}

	public ExtractorResult() {
		this.hypotheses = new ArrayList<>();
	}

	public StateMachine getLearnedModel() {
		return learnedModel;
	}

	private void setLearnedModel(StateMachine learnedModel) {
		this.learnedModel = learnedModel;
	}

	public Statistics getStatistics() {
		return statistics;
	}
	
	private void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

	private void addHypothesis(StateMachine hypothesis) {
		hypotheses.add(hypothesis);
	}

	public List<StateMachine> getHypotheses() {
		return Collections.unmodifiableList(hypotheses);
	}
}
}
