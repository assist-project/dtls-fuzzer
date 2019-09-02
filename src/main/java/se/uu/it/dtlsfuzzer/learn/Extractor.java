package se.uu.it.dtlsfuzzer.learn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.io.Files;

import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;
import se.uu.it.dtlsfuzzer.execute.BasicInputExecutor;
import se.uu.it.dtlsfuzzer.sut.CachingSULOracle;
import se.uu.it.dtlsfuzzer.sut.ExperimentTimeoutException;
import se.uu.it.dtlsfuzzer.sut.IsAliveWrapper;
import se.uu.it.dtlsfuzzer.sut.LoggingSULOracle;
import se.uu.it.dtlsfuzzer.sut.NonDeterminismRetryingSULOracle;
import se.uu.it.dtlsfuzzer.sut.ObservationTree;
import se.uu.it.dtlsfuzzer.sut.ResettingWrapper;
import se.uu.it.dtlsfuzzer.sut.TimeoutWrapper;
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
	public static final String SUL_CONFIG_FILENAME = "sul.config";
	public static final String ALPHABET_FILENAME = "alphabet.xml";
	public static final String NON_DET_FILENAME = "nondet.log";
	private static final String ERROR_FILENAME = "error.msg";

	/*
	 * In case of non-determinism, the number of times the causing sequence of
	 * inputs (to confirm/infirm non-determinism)
	 */
	private static final Integer NON_DET_ATTEMPTS = 3;

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

		// for convenience, we copy all the input files/streams
		// to the output folder before starting the arduous learning process
		copyInputsToOutputFolder(outputFolder);

		// setting up SUL/T (System Under Learning/Test)
		SUL<TlsInput, TlsOutput> tlsSystemUnderTest = new TlsSUL(
				finderConfig.getSulDelegate(), new BasicInputExecutor());

		if (finderConfig.getSulDelegate().getCommand() != null) {
			tlsSystemUnderTest = new TlsProcessWrapper(tlsSystemUnderTest,
					finderConfig.getSulDelegate());
		}
		if (finderConfig.getSulDelegate().getResetPort() != null) {
			tlsSystemUnderTest = new ResettingWrapper<TlsInput, TlsOutput>(
					tlsSystemUnderTest, finderConfig.getSulDelegate());
		}
		if (finderConfig.getLearningConfig().getTimeLimit() != null) {
			tlsSystemUnderTest = new TimeoutWrapper<TlsInput, TlsOutput>(
					tlsSystemUnderTest, finderConfig.getLearningConfig()
							.getTimeLimit());
		}
		tlsSystemUnderTest = new IsAliveWrapper(tlsSystemUnderTest);

		SymbolCounterSUL<TlsInput, TlsOutput> symbolCounterSul = new SymbolCounterSUL<>(
				"symbol counter", tlsSystemUnderTest);
		ResetCounterSUL<TlsInput, TlsOutput> resetCounterSul = new ResetCounterSUL<>(
				"reset counter", symbolCounterSul);

		StatisticsTracker tracker = new StatisticsTracker(
				symbolCounterSul.getStatisticalData(),
				resetCounterSul.getStatisticalData());
		tlsSystemUnderTest = resetCounterSul;

		MealyMembershipOracle<TlsInput, TlsOutput> sulOracle = new SULOracle<TlsInput, TlsOutput>(
				tlsSystemUnderTest);

		// TODO the LOGGER instances should handle this, instead of us passing
		// non det writers as arguments.
		FileWriter nonDetWriter = null;
		try {
			nonDetWriter = new FileWriter(new File(outputFolder,
					NON_DET_FILENAME));
		} catch (IOException e1) {
			throw new RuntimeException(
					"Could not create non-determinism file writer");
		}

		if (finderConfig.getLearningConfig().getRunsPerMembershipQuery() > 1) {
			sulOracle = new MultipleRunsSULOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, TlsOutput>(
					finderConfig.getLearningConfig()
							.getRunsPerMembershipQuery(), sulOracle, true,
					nonDetWriter);
		}

		// the cache is an observation tree
		ObservationTree<TlsInput, TlsOutput> cache = new ObservationTree<>();

		// we are adding a cache so that executions of same inputs aren't
		// repeated
		CachingSULOracle<TlsInput, TlsOutput> cachedSulOracle = new CachingSULOracle<TlsInput, TlsOutput>(
				new SULOracle<TlsInput, TlsOutput>(tlsSystemUnderTest), cache,
				false, TlsOutput.socketClosed());

		// a SUL oracle which uses the cached oracle and attempts to re-run
		// queries in case non-determinism is detected
		MealyMembershipOracle<TlsInput, TlsOutput> learningSulOracle = new NonDeterminismRetryingSULOracle<TlsInput, TlsOutput>(
				cachedSulOracle, NON_DET_ATTEMPTS, nonDetWriter);

		if (finderConfig.getLearningConfig().getQueryFile() != null) {
			FileWriter queryWriter = null;
			try {
				queryWriter = new FileWriter(new File(outputFolder,
						finderConfig.getLearningConfig().getQueryFile()));
			} catch (IOException e1) {
				throw new RuntimeException("Could not create queryfile writer");
			}
			learningSulOracle = new LoggingSULOracle<TlsInput, TlsOutput>(
					learningSulOracle, queryWriter);
		}

		// setting up membership and equivalence oracles
		MealyLearner<TlsInput, TlsOutput> algorithm = LearnerFactory
				.loadLearner(finderConfig.getLearningConfig(),
						learningSulOracle, alphabet);

		MealyMembershipOracle<TlsInput, TlsOutput> testOracle = new SULOracle<TlsInput, TlsOutput>(
				tlsSystemUnderTest);

		// in case sanitization is enabled, we apply a CE verification wrapper
		// to
		// check counterexamples before they are returned to the EQ oracle
		if (finderConfig.getLearningConfig().isCeSanitization()) {
			testOracle = new CESanitizingSULOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, TlsOutput>(
					finderConfig.getLearningConfig().getCeReruns(), testOracle,
					() -> algorithm.getHypothesisModel(), finderConfig
							.getLearningConfig().isProbabilisticSanitization(),
					nonDetWriter);
		}

		testOracle = new CachingSULOracle<TlsInput, TlsOutput>(testOracle,
				cache, finderConfig.getLearningConfig().dontCacheTests(),
				TlsOutput.socketClosed());

		EquivalenceOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, Word<TlsOutput>> equivalenceAlgorithm = LearnerFactory
				.loadTester(finderConfig.getLearningConfig(),
						tlsSystemUnderTest, testOracle, alphabet);

		// running learning and collecting important statistics
		MealyMachine<?, TlsInput, ?, TlsOutput> hypothesis = null;
		DefaultQuery<TlsInput, Word<TlsOutput>> counterExample = null;
		boolean success = false;
		int rounds = 0;

		algorithm.startLearning();
		tracker.startLearning(finderConfig, alphabet);
		try {
			do {
				hypothesis = algorithm.getHypothesisModel();
				StateMachine stateMachine = new StateMachine(hypothesis,
						alphabet);
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
			success = true;
		} catch (ExperimentTimeoutException exc) {
			LOG.severe("Learning timed out after a duration of "
					+ exc.getDuration() + " (i.e. "
					+ exc.getDuration().toHours() + " hours )");
			LOG.severe("Logging last hypothesis");
		} catch (Exception exc) {
			LOG.severe("Exception generated during learning");
			// useful to log what actually went wrong
			try (FileWriter fw = new FileWriter(new File(outputFolder,
					ERROR_FILENAME))) {
				PrintWriter pw = new PrintWriter(fw);
				pw.println(exc.getMessage());
				exc.printStackTrace(pw);
				pw.close();
			} catch (IOException e) {
				LOG.severe("Could not create error file writer");
			}
		}

		// building results:
		StateMachine stateMachine = new StateMachine(hypothesis, alphabet);
		tracker.finishedLearning(stateMachine, success);
		Statistics statistics = tracker.generateStatistics();

		LOG.log(Level.INFO, "Finished Learning");
		LOG.log(Level.INFO, "Number of Rounds:" + rounds);
		LOG.log(Level.INFO, statistics.toString());

		extractorResult.setLearnedModel(stateMachine);
		extractorResult.setStatistics(statistics);

		if (success) {
			// exporting to output files
			serializeHypothesis(stateMachine, outputFolder,
					LEARNED_MODEL_FILENAME, true, false);

			// we disable this feature for now, as models are too large for it
			// serializeHypothesis(stateMachine, outputFolder,
			// LEARNED_MODEL_FILENAME.replace(".dot", "FullOutput.dot"),
			// false, true);

			extractorResult.setLearnedModelFile(new File(outputFolder,
					LEARNED_MODEL_FILENAME));
		}
		try {
			statistics.export(new FileWriter(new File(outputFolder,
					STATISTICS_FILENAME)));
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Could not copy statistics to output folder");
		}

		return extractorResult;
	}

	private void copyInputsToOutputFolder(File outputFolder) {
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
		}
		try {
			dumpToFile(finderConfig.getSulDelegate().getSulConfigInputStream(),
					new File(outputFolder, SUL_CONFIG_FILENAME));
		} catch (IOException e) {
			LOG.log(Level.SEVERE,
					"Could not copy sul configuration to output folder");
		}
	}

	private void dumpToFile(InputStream is, File outputFile) throws IOException {
		InputStream inputStream = finderConfig.getSulDelegate()
				.getSulConfigInputStream();
		try (FileOutputStream fw = new FileOutputStream(outputFile)) {
			byte[] bytes = new byte[1000];
			while (inputStream.read(bytes) > 0) {
				fw.write(bytes);
			}
		} finally {
			inputStream.close();
		}
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
