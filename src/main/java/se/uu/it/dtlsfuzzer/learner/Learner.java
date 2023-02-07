package se.uu.it.dtlsfuzzer.learner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;

import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.CleanupTasks;
import se.uu.it.dtlsfuzzer.config.LearningConfig;
import se.uu.it.dtlsfuzzer.config.StateFuzzerConfig;
import se.uu.it.dtlsfuzzer.mapper.PhasedMapper;
import se.uu.it.dtlsfuzzer.sut.CachingSULOracle;
import se.uu.it.dtlsfuzzer.sut.ExperimentTimeoutException;
import se.uu.it.dtlsfuzzer.sut.LoggingSULOracle;
import se.uu.it.dtlsfuzzer.sut.MultipleRunsSULOracle;
import se.uu.it.dtlsfuzzer.sut.NonDeterminismRetryingSULOracle;
import se.uu.it.dtlsfuzzer.sut.ObservationTree;
import se.uu.it.dtlsfuzzer.sut.TestLimitReachedException;
import se.uu.it.dtlsfuzzer.sut.TlsSULBuilder;
import se.uu.it.dtlsfuzzer.sut.input.AlphabetFactory;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

/**
 * Taken/adapted from StateVulnFinder tool (Extractor Component).
 */
public class Learner {

	public static final String LEARNED_MODEL_FILENAME = "learnedModel.dot";
	public static final String STATISTICS_FILENAME = "statistics.txt";
	public static final String SUL_CONFIG_FILENAME = "sul.config";
	public static final String ALPHABET_FILENAME = "alphabet.xml";
	public static final String NON_DET_FILENAME = "nondet.log";
	private static final String ERROR_FILENAME = "error.msg";
	private static final String LEARNING_STATE_FILENAME = "state.log";

	private static final Logger LOGGER = LogManager.getLogger();
	private final StateFuzzerConfig fuzzerConfig;
	private final LearningConfig learningConfig;
	private final Alphabet<TlsInput> alphabet;
	private final CleanupTasks cleanupTasks;

	public Learner(StateFuzzerConfig fuzzerConfig, Alphabet<TlsInput> alphabet, CleanupTasks tasks) {
		this.fuzzerConfig = fuzzerConfig;
		this.learningConfig = fuzzerConfig.getLearningConfig();
		this.alphabet = alphabet;
		this.cleanupTasks = tasks;
	}

	public LearnerResult inferStateMachine() {
		LearnerResult learnerResult = new LearnerResult();

		// setting up our output directory
		File outputFolder = new File(fuzzerConfig.getOutput());
		outputFolder.mkdirs();

		// for convenience, we copy all the input files/streams
		// to the output folder before starting the arduous learning process
		copyInputsToOutputFolder(outputFolder);

		// setting up SUL/T (System Under Learning/Test)

		TlsSULBuilder sulBuilder = new TlsSULBuilder(fuzzerConfig.getSulDelegate(), fuzzerConfig.getMapperConfig(),
				new PhasedMapper(fuzzerConfig.getMapperConfig()), cleanupTasks);

		if (learningConfig.getTimeLimit() != null) {
			sulBuilder.setTimeLimit(learningConfig.getTimeLimit());
		}

		if (learningConfig.getTestLimit() != null) {
			sulBuilder.setTestLimit(learningConfig.getTestLimit());
		}

		SUL<TlsInput, TlsOutput> tlsSystemUnderTest = sulBuilder.getWrappedTlsSUL();

		StatisticsTracker tracker = new StatisticsTracker(sulBuilder.getInputCounter(), sulBuilder.getTestCounter());

		MealyMembershipOracle<TlsInput, TlsOutput> learningSulOracle = new SULOracle<TlsInput, TlsOutput>(
				tlsSystemUnderTest);

		// TODO the LOGGER instances should handle this, instead of us passing
		// non det writers as arguments.
		FileWriter nonDetWriter = null;
		try {
			nonDetWriter = new FileWriter(new File(outputFolder, NON_DET_FILENAME));
		} catch (IOException e1) {
			throw new RuntimeException("Could not create non-determinism file writer");
		}
		if (learningConfig.getRunsPerMembershipQuery() > 1) {
			learningSulOracle = new MultipleRunsSULOracle<TlsInput, TlsOutput>(
					learningConfig.getRunsPerMembershipQuery(), learningSulOracle, true,
					nonDetWriter);
		}

		// the cache is an observation tree
		ObservationTree<TlsInput, TlsOutput> cache = new ObservationTree<>();

		// a SUL oracle which uses the cache to check for non-determinism
		// and re-runs queries if non-det is detected
		learningSulOracle = new NonDeterminismRetryingSULOracle<TlsInput, TlsOutput>(learningSulOracle, cache,
				learningConfig.getMembershipQueryRetries(), true, nonDetWriter);

		// we are adding a cache so that executions of same inputs aren't
		// repeated
		if (fuzzerConfig.getMapperConfig().isSocketClosedAsTimeout()) {
			learningSulOracle = new CachingSULOracle<TlsInput, TlsOutput>(learningSulOracle, cache, false);
		} else {
			learningSulOracle = new CachingSULOracle<TlsInput, TlsOutput>(learningSulOracle, cache, false,
					TlsOutput.socketClosed());
		}

		if (learningConfig.getQueryFile() != null) {
			FileWriter queryWriter = null;
			try {
				queryWriter = new FileWriter(new File(outputFolder, learningConfig.getQueryFile()));
			} catch (IOException e1) {
				throw new RuntimeException("Could not create queryfile writer");
			}
			learningSulOracle = new LoggingSULOracle<TlsInput, TlsOutput>(learningSulOracle, queryWriter);
		}

		// setting up membership and equivalence oracles
		MealyLearner<TlsInput, TlsOutput> algorithm = LearnerFactory.loadLearner(learningConfig,
				learningSulOracle, alphabet);

		MealyMembershipOracle<TlsInput, TlsOutput> testOracle = new SULOracle<TlsInput, TlsOutput>(tlsSystemUnderTest);

		// in case sanitization is enabled, we apply a CE verification wrapper
		// to
		// check counterexamples before they are returned to the EQ oracle
		if (learningConfig.isCeSanitization()) {
			testOracle = new CESanitizingSULOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, TlsOutput>(
					learningConfig.getCeReruns(), testOracle, () -> algorithm.getHypothesisModel(),
					cache, learningConfig.isProbabilisticSanitization(),
					learningConfig.isSkipNonDetTests(), nonDetWriter);
		}

		if (fuzzerConfig.getMapperConfig().isSocketClosedAsTimeout()) {
			testOracle = new CachingSULOracle<TlsInput, TlsOutput>(testOracle, cache,
					!learningConfig.isCacheTests());
		} else {
			testOracle = new CachingSULOracle<TlsInput, TlsOutput>(testOracle, cache,
					!learningConfig.isCacheTests(), TlsOutput.socketClosed());
		}

		EquivalenceOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, Word<TlsOutput>> equivalenceAlgorithm = LearnerFactory
				.loadTester(learningConfig, tlsSystemUnderTest, testOracle, alphabet);

		// running learning and collecting important statistics
		MealyMachine<?, TlsInput, ?, TlsOutput> hypothesis = null;
		StateMachine stateMachine = null;
		DefaultQuery<TlsInput, Word<TlsOutput>> counterExample = null;
		boolean finished = false, aborted = false;
		String notFinishedReason = null;
		int rounds = 0;

		try {
			tracker.setRuntimeStateTracking(new FileOutputStream(new File(outputFolder, LEARNING_STATE_FILENAME)));
		} catch (FileNotFoundException e1) {
			throw new RuntimeException("Could not create runtime state tracking output stream");
		}
		tracker.startLearning(fuzzerConfig, alphabet);
		LOGGER.info("Input alphabet: {}", alphabet);
		LOGGER.info("Starting learning");
		try {
			algorithm.startLearning();
			rounds ++;

			do {
				hypothesis = algorithm.getHypothesisModel();
				stateMachine = new StateMachine(hypothesis, alphabet);
				learnerResult.addHypothesis(stateMachine);
				String hypName = "hyp" + rounds + ".dot";
				// it is useful to print intermediate hypothesis as learning is
				// running
				serializeHypothesis(stateMachine, outputFolder, hypName, false);
				LOGGER.info("Generated new hypothesis: " + hypName);
				tracker.newHypothesis(stateMachine);

				if (learningConfig.getRoundLimit() != null
				        && learningConfig.getRoundLimit() == rounds) {
				    LOGGER.info("Learning exhausted the number of hypothesis construction rounds allowed ({})", learningConfig.getRoundLimit());
				    notFinishedReason = "hypothesis construction round limit reached";
				    aborted = true;
				    break;
				}

				LOGGER.info("Validating hypothesis");
				counterExample = equivalenceAlgorithm.findCounterExample(hypothesis, alphabet);

				if (counterExample != null) {
					LOGGER.info("Counterexample: " + counterExample.toString());
					tracker.newCounterExample(counterExample);
					// we create a copy, since the hypothesis reference will not be valid after
					// refinement and we may still need it (if learning abruptly terminates)
					stateMachine = stateMachine.copy();
					LOGGER.info("Refining hypothesis");
					algorithm.refineHypothesis(counterExample);
	                rounds++;
				}
			} while (counterExample != null);
			finished = !aborted;
		} catch (ExperimentTimeoutException exc) {
			LOGGER.warn("Learning timed out after a duration of {} (i.e. "
					+ exc.getDuration().toHours() + " hours, or {} minutes)", exc.getDuration(), exc.getDuration().toMinutes() );
			notFinishedReason = "learning timed out";
		} catch (TestLimitReachedException exc) {
			LOGGER.warn("Learning exhausted the number of tests allowed ({} tests)", exc.getQueryLimit());
			notFinishedReason = "query limit reached";
		} catch (Exception exc) {
			notFinishedReason = exc.getMessage();
			LOGGER.error("Exception generated during learning");
			LOGGER.error(exc);
			exc.printStackTrace();

			// useful to log what actually went wrong
			try (FileWriter fw = new FileWriter(new File(outputFolder, ERROR_FILENAME))) {
				PrintWriter pw = new PrintWriter(fw);
				pw.println(exc.getMessage());
				exc.printStackTrace(pw);
				pw.close();
			} catch (IOException e) {
				LOGGER.error("Could not create error file writer");
			}
		}

		// building results:
		tracker.finishedLearning(stateMachine, finished, notFinishedReason);

		LOGGER.info("Finished experiment");
		LOGGER.info("Number of refinement rounds:" + rounds);
		LOGGER.info("Results stored in {}", outputFolder.getPath());
		if (stateMachine == null) {
			LOGGER.info("Could not generate a first hypothesis, so not much to report on");
			if (notFinishedReason != null) {
				LOGGER.info("Potential cause: {}", notFinishedReason);
			}
			return null;
		}

		Statistics statistics = tracker.generateStatistics();
		LOGGER.info(statistics);

		learnerResult.setLearnedModel(stateMachine);
		learnerResult.setStatistics(statistics);

		// exporting to output files
		serializeHypothesis(stateMachine, outputFolder, LEARNED_MODEL_FILENAME, true);

		// we disable this feature for now, as models are too large for it
		// serializeHypothesis(stateMachine, outputFolder,
		// LEARNED_MODEL_FILENAME.replace(".dot", "FullOutput.dot"),
		// false, true);

		learnerResult.setLearnedModelFile(new File(outputFolder, LEARNED_MODEL_FILENAME));

		try {
			statistics.export(new FileWriter(new File(outputFolder, STATISTICS_FILENAME)));
		} catch (IOException e) {
			LOGGER.error("Could not copy statistics to output folder");
		}

		return learnerResult;
	}

	private void copyInputsToOutputFolder(File outputFolder) {
		try {
			Files.copy(AlphabetFactory.getAlphabetFile(fuzzerConfig), new File(outputFolder, ALPHABET_FILENAME));
		} catch (IOException e) {
			LOGGER.error("Could not copy alphabet to output folder");
		}
		if (learningConfig.getEquivalenceAlgorithms()
				.contains(EquivalenceAlgorithmName.SAMPLED_TESTS)) {
			try {
				Files.copy(new File(learningConfig.getTestFile()),
						new File(outputFolder, learningConfig.getTestFile()));
			} catch (IOException e) {
				LOGGER.error("Could not copy sampled tests file to output folder");
			}
		}
		try {
			dumpToFile(fuzzerConfig.getSulDelegate().getSulConfigInputStream(),
					new File(outputFolder, SUL_CONFIG_FILENAME));
		} catch (IOException e) {
			LOGGER.error("Could not copy sul configuration to output folder");
		}
	}

	private void dumpToFile(InputStream is, File outputFile) throws IOException {
		InputStream inputStream = fuzzerConfig.getSulDelegate().getSulConfigInputStream();
		try (FileOutputStream fw = new FileOutputStream(outputFile)) {
			byte[] bytes = new byte[1000];
			int read;
			while ((read = inputStream.read(bytes)) > 0) {
				fw.write(bytes, 0, read);
			}
		} finally {
			inputStream.close();
		}
	}

	private void serializeHypothesis(StateMachine hypothesis, File folder, String name, boolean genPdf) {
		File graphFile = new File(folder, name);
		hypothesis.export(graphFile, genPdf);
	}

	public static class LearnerResult {

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

		public LearnerResult() {
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