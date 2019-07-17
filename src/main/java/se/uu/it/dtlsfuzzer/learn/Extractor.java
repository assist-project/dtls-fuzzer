package se.uu.it.dtlsfuzzer.learn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.io.Files;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.SUL;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SymbolCounterSUL;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;
import se.uu.it.dtlsfuzzer.execute.BasicInputExecutor;
import se.uu.it.dtlsfuzzer.sut.CachingSULOracle;
import se.uu.it.dtlsfuzzer.sut.NonDeterminismRetryingSUL;
import se.uu.it.dtlsfuzzer.sut.TlsProcessWrapper;
import se.uu.it.dtlsfuzzer.sut.TlsSUL;
import se.uu.it.dtlsfuzzer.sut.io.AlphabetFactory;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

/**
 * Taken/adapted from StateVulnFinder tool.
 */
public class Extractor {

	public static final String LEARNED_MODEL_FILENAME = "learnedModel.dot";
	public static final String STATISTICS_FILENAME = "statistics.txt";
	public static final String ALPHABET_FILENAME = "alphabet.xml";
	public static final String NON_DET_FILENAME = "nondet.log";
	/*
	 * In case of non-determinism, the number of times the causing sequence of
	 * inputs (to confirm/infirm non-determinism)
	 */
	private static final Integer NON_DET_ATTEMPTS = 5;

	private static final Logger LOG = Logger.getLogger(Extractor.class
			.getName());
	private final DtlsFuzzerConfig finderConfig;
	private final Alphabet<TlsInput> alphabet;

	public Extractor(DtlsFuzzerConfig finderConfig, Alphabet<TlsInput> alphabet) {
		this.finderConfig = finderConfig;
		this.alphabet = alphabet;
	}

	public ExtractorResult extractStateMachine() {
		ExtractorResult extractorResult = new ExtractorResult();

		// setting up our output directory
		File outputFolder = new File(finderConfig.getOutput());
		outputFolder.mkdirs();

		// setting up SUL/T (System Under Learning/Test)
		SUL<TlsInput, TlsOutput> tlsSystemUnderTest = new TlsSUL(
				finderConfig.getSulDelegate(), new BasicInputExecutor());

		if (finderConfig.getSulDelegate().getCommand() != null) {
			tlsSystemUnderTest = new TlsProcessWrapper(tlsSystemUnderTest,
					finderConfig.getSulDelegate());
		}

		// we use a wrapper to check for non-determinism, we could use its
		// observation tree as cache
		try {
			tlsSystemUnderTest = new NonDeterminismRetryingSUL<TlsInput, TlsOutput>(
					tlsSystemUnderTest, NON_DET_ATTEMPTS, new FileWriter(
							new File(outputFolder, NON_DET_FILENAME)));
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
		MealyLearner<TlsInput, TlsOutput> algorithm = LearnerFactory
				.loadLearner(finderConfig.getLearningConfig(), sulOracle,
						alphabet);
		EquivalenceOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, Word<TlsOutput>> equivalenceAlgorithm = LearnerFactory
				.loadTester(finderConfig.getLearningConfig(),
						tlsSystemUnderTest, sulOracle, alphabet);

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
			serializeHypothesis(stateMachine, outputFolder, "hyp"
					+ (rounds + 1) + ".dot", false, false);

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

		LOG.log(Level.INFO, "Finished Learning");
		LOG.log(Level.INFO, "Number of Rounds:" + rounds);
		LOG.log(Level.INFO, statistics.toString());

		extractorResult.setLearnedModel(stateMachine);
		extractorResult.setStatistics(statistics);

		// exporting to output files
		serializeHypothesis(stateMachine, outputFolder, LEARNED_MODEL_FILENAME,
				true, false);
		serializeHypothesis(stateMachine, outputFolder,
				LEARNED_MODEL_FILENAME.replace(".dot", "FullOutput.dot"),
				false, true);

		extractorResult.setLearnedModelFile(new File(outputFolder,
				LEARNED_MODEL_FILENAME));
		try {
			Files.copy(AlphabetFactory.getAlphabetFile(finderConfig), new File(
					outputFolder, ALPHABET_FILENAME));
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Could not copy alphabet to output folder");
		}
		if (finderConfig.getLearningConfig().getEquivalenceAlgorithms()
				.contains(EquivalenceAlgorithmName.SAMPLED_TESTS)) {
			try {
				Files.copy(new File(finderConfig.getLearningConfig()
						.getTestFile()), new File(outputFolder, finderConfig
						.getLearningConfig().getTestFile()));
			} catch (IOException e) {
				LOG.log(Level.SEVERE,
						"Could not copy sampled tests file to output folder");
			}
			try {
				Files.copy(new File(finderConfig.getSulDelegate()
						.getSulConfig()), new File(outputFolder, finderConfig
						.getSulDelegate().getSulConfig()));
			} catch (IOException e) {
				LOG.log(Level.SEVERE,
						"Could not copy sul configuration to output folder");
			}
		}
		try {
			statistics.export(new FileWriter(new File(outputFolder,
					STATISTICS_FILENAME)));
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Could not copy statistics to output folder");
		}

		return extractorResult;
	}

	private void serializeHypothesis(StateMachine hypothesis, File folder,
			String name, boolean genPdf, boolean fullOutput) {
		File graphFile = new File(folder, name);
		hypothesis.export(graphFile, genPdf, fullOutput);
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
