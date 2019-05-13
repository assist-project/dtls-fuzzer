package se.uu.it.modeltester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;

import de.learnlib.api.SUL;
import de.learnlib.oracles.SULOracle;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.modeltester.config.ModelBasedTesterConfig;
import se.uu.it.modeltester.learn.Extractor;
import se.uu.it.modeltester.learn.Extractor.ExtractorResult;
import se.uu.it.modeltester.sut.ProcessHandler;
import se.uu.it.modeltester.sut.SulProcessWrapper;
import se.uu.it.modeltester.sut.TlsSUL;
import se.uu.it.modeltester.sut.io.AlphabetFactory;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;
import se.uu.it.modeltester.sut.io.definitions.Definitions;
import se.uu.it.modeltester.sut.io.definitions.DefinitionsFactory;
import se.uu.it.modeltester.test.ConformanceTester;
import se.uu.it.modeltester.test.TestingReport;

public class ModelBasedTester {
	private static final Logger LOGGER = LogManager.getLogger(ModelBasedTester.class);
	public static final String TEST_REPORT_FILENAME = "testingReport.txt";
	
	private ModelBasedTesterConfig config;

	public ModelBasedTester(ModelBasedTesterConfig config) {
		this.config = config;
	}
	

	public TestingReport startTesting() throws ParseException, IOException {
		// setting up our output directory
		File folder = new File(config.getOutput());
		folder.mkdirs();
		SULOracle<TlsInput, TlsOutput> sutOracle = createOracle(config);
		if (config.getTraceRunnerConfig().getTrace() != null) {
			runTrace(config, sutOracle);
			System.exit(0);
		}
		if (config.isOnlyLearn()) {
			extractModel(config);
			return null;
		} else {
			ConformanceTestingTask task = generateModelBasedTestingTask(config);
			TestingReport report = testModel(config, sutOracle, task);
			logResult(report, config);
			return report;
		}
	}
	

	private void runTrace(ModelBasedTesterConfig config, SULOracle<TlsInput, TlsOutput> sutOracle) throws IOException, ParseException {
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
		TraceRunner runner = new TraceRunner(config.getTraceRunnerConfig(), alphabet, sutOracle);
		TraceRunnerResult result = runner.runTrace();
		if (config.getSpecification() != null) {
			Definitions definitions = DefinitionsFactory.generateDefinitions(alphabet);
			MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(new TlsProcessor(definitions));
			FastMealy<TlsInput, TlsOutput> machine = dotParser.parseAutomaton(config.getSpecification()).get(0);
			Word<TlsOutput> outputWord = machine.computeOutput(result.getInputWord());
			LOGGER.error("Expected output: " + outputWord);
		}
	}


	private void logResult(TestingReport report, ModelBasedTesterConfig config) throws IOException {
		if (config.getOutput() == null) {
			report.printReport(System.out);
		} else {
			File testReport = Paths.get(config.getOutput(), TEST_REPORT_FILENAME).toFile();
			testReport.createNewFile();
			PrintStream ps = new PrintStream(new FileOutputStream(testReport));
			report.printReport(ps);
		}
	}


	public SULOracle<TlsInput, TlsOutput> createOracle(ModelBasedTesterConfig config) {
		SUL<TlsInput, TlsOutput> tlsSut = new TlsSUL(config.getSulDelegate());
		if (config.getSulDelegate().getCommand() != null) {
			tlsSut = new SulProcessWrapper<>(tlsSut, 
					new ProcessHandler(config.getSulDelegate().getCommand(), 
							config.getSulDelegate().getRunWait()));
		}
		SULOracle<TlsInput, TlsOutput> tlsOracle = new SULOracle<TlsInput, TlsOutput>(tlsSut);
		return tlsOracle;
	}
	
	/**
	 * Generates a model based testing task, that is, a specification and an alphabet to focus model based testing on.
	 * If a specification is not provided, it is created using active learning. 
	 */
	public ConformanceTestingTask generateModelBasedTestingTask(ModelBasedTesterConfig config) throws FileNotFoundException, ParseException {
		
		String specification = config.getSpecification(); 
		if (specification == null) {
			ExtractorResult result = extractModel(config);
			// TODO not the nicest way of making a conversion
			MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(new TlsProcessor(result.getLearnedModel().generateDefinitions()));
			FastMealy<TlsInput, TlsOutput> fastMealy = dotParser.parseAutomaton(result.getLearnedModelFile().getPath()).get(0);
			return new ConformanceTestingTask( fastMealy, fastMealy.getInputAlphabet());
		} else {
			Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
			Definitions definitions = DefinitionsFactory.generateDefinitions(alphabet);
			MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(new TlsProcessor(definitions));
			FastMealy<TlsInput, TlsOutput>  model = dotParser.parseAutomaton(specification).get(0);
			return new ConformanceTestingTask(model, alphabet);
		}
	}
	
	private ExtractorResult extractModel(ModelBasedTesterConfig config) {
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
		Extractor extractor = new Extractor(config, alphabet);
		ExtractorResult result = extractor.extractStateMachine();
		return result;
	}
	
	private TestingReport testModel(ModelBasedTesterConfig config, SULOracle<TlsInput, TlsOutput> sutOracle, ConformanceTestingTask task) {
		ConformanceTester tester = new ConformanceTester(config.getTestingConfig());
		return tester.testModel(sutOracle, task);
	}
}
