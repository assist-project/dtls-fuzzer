package se.uu.it.dtlsfuzzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;
import se.uu.it.dtlsfuzzer.execute.TestingInputExecutor;
import se.uu.it.dtlsfuzzer.learn.Extractor;
import se.uu.it.dtlsfuzzer.learn.Extractor.ExtractorResult;
import se.uu.it.dtlsfuzzer.sut.IsAliveWrapper;
import se.uu.it.dtlsfuzzer.sut.ResettingWrapper;
import se.uu.it.dtlsfuzzer.sut.TlsProcessWrapper;
import se.uu.it.dtlsfuzzer.sut.TlsSUL;
import se.uu.it.dtlsfuzzer.sut.io.AlphabetFactory;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;
import se.uu.it.dtlsfuzzer.sut.io.definitions.Definitions;
import se.uu.it.dtlsfuzzer.sut.io.definitions.DefinitionsFactory;
import se.uu.it.dtlsfuzzer.test.ConformanceTester;
import se.uu.it.dtlsfuzzer.test.TestReport;

public class DtlsFuzzer {
	private static final Logger LOGGER = LogManager.getLogger(DtlsFuzzer.class);

	private DtlsFuzzerConfig config;
	private CleanupTasks cleanupTasks;

	public DtlsFuzzer(DtlsFuzzerConfig config) {
		this.config = config;
		this.cleanupTasks = new CleanupTasks();
	}

	public TestReport startTesting() throws ParseException, IOException {
		TestReport report = null;
		try {
			if (config.getTestRunnerConfig().getTest() != null) {
				runTest(config);
			} else {
				// setting up our output directory
				File folder = new File(config.getOutput());
				folder.mkdirs();
				if (config.isOnlyLearn()) {
					extractModel(config);
				} else {
					ConformanceTestingTask task = generateModelBasedTestingTask(config);
					report = testModel(config, task);
				}
			}
		} catch (Exception e) {
			cleanupTasks.execute();
			throw e;
		}
		cleanupTasks.execute();

		return report;
	}

	private void runTest(DtlsFuzzerConfig config) throws IOException,
			ParseException {
		MealyMembershipOracle<TlsInput, TlsOutput> sutOracle = createTestOracle(config);
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
		TestRunner runner = new TestRunner(config.getTestRunnerConfig(),
				alphabet, sutOracle);
		List<TestRunnerResult<TlsInput, TlsOutput>> results = runner.runTests();
		for (TestRunnerResult<TlsInput, TlsOutput> result : results) {
			LOGGER.error(result.toString());
			if (config.getSpecification() != null) {
				Definitions definitions = DefinitionsFactory
						.generateDefinitions(alphabet);
				MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(
						new TlsProcessor(definitions));
				FastMealy<TlsInput, TlsOutput> machine = dotParser
						.parseAutomaton(config.getSpecification()).get(0);
				Word<TlsOutput> outputWord = machine.computeOutput(result
						.getInputWord());
				LOGGER.error("Expected output: " + outputWord);
			}
		}
	}

	public MealyMembershipOracle<TlsInput, TlsOutput> createTestOracle(
			DtlsFuzzerConfig config) {
		SUL<TlsInput, TlsOutput> tlsSut = new TlsSUL(config.getSulDelegate(),
				new TestingInputExecutor());
		if (config.getSulDelegate().getCommand() != null) {

			tlsSut = new TlsProcessWrapper(tlsSut, config.getSulDelegate());
		}
		if (config.getSulDelegate().getResetPort() != null) {
			tlsSut = new ResettingWrapper<TlsInput, TlsOutput>(tlsSut,
					config.getSulDelegate(), cleanupTasks);
		}
		tlsSut = new IsAliveWrapper(tlsSut);

		MealyMembershipOracle<TlsInput, TlsOutput> tlsOracle = new SULOracle<TlsInput, TlsOutput>(
				tlsSut);

		return tlsOracle;
	}

	/**
	 * Generates a model based testing task, that is, a specification and an
	 * alphabet to focus model based testing on. If a specification is not
	 * provided, it is created using active learning.
	 */
	private ConformanceTestingTask generateModelBasedTestingTask(
			DtlsFuzzerConfig config) throws FileNotFoundException,
			ParseException {

		String specification = config.getSpecification();
		if (specification == null) {
			ExtractorResult result = extractModel(config);
			// TODO not the nicest way of making a conversion
			MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(
					new TlsProcessor(result.getLearnedModel()
							.generateDefinitions()));
			FastMealy<TlsInput, TlsOutput> fastMealy = dotParser
					.parseAutomaton(result.getLearnedModelFile().getPath())
					.get(0);
			return new ConformanceTestingTask(fastMealy,
					fastMealy.getInputAlphabet());
		} else {
			Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
			Definitions definitions = DefinitionsFactory
					.generateDefinitions(alphabet);
			MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(
					new TlsProcessor(definitions));
			FastMealy<TlsInput, TlsOutput> model = dotParser.parseAutomaton(
					specification).get(0);
			if (!model.getInputAlphabet().containsAll(alphabet)) {
				LOGGER.error("The configured alphabet contains inputs not included in the specification. "
						+ "These inputs will be excluded from the testing alphabet.");
				alphabet = new ListAlphabet<TlsInput>(alphabet.stream()
						.filter(i -> model.getInputAlphabet().contains(i))
						.collect(Collectors.toList()));
			}
			return new ConformanceTestingTask(model, alphabet);
		}
	}

	private ExtractorResult extractModel(DtlsFuzzerConfig config) {
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
		Extractor extractor = new Extractor(config, alphabet, cleanupTasks);
		ExtractorResult result = extractor.extractStateMachine();
		return result;
	}

	private TestReport testModel(DtlsFuzzerConfig config,
			ConformanceTestingTask task) throws IOException {
		MealyMembershipOracle<TlsInput, TlsOutput> sutOracle = createTestOracle(config);
		ConformanceTester tester = new ConformanceTester(config);
		return tester.testModel(sutOracle, task);
	}
}
