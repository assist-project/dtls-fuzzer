package se.uu.it.modeltester;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;

import de.learnlib.api.SUL;
import de.learnlib.oracles.SULOracle;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import se.uu.it.modeltester.config.ModelBasedTesterConfig;
import se.uu.it.modeltester.learn.Extractor;
import se.uu.it.modeltester.learn.Extractor.ExtractorResult;
import se.uu.it.modeltester.sut.ProcessHandler;
import se.uu.it.modeltester.sut.SulProcessWrapper;
import se.uu.it.modeltester.sut.TlsSUL;
import se.uu.it.modeltester.sut.io.AlphabetFactory;
import se.uu.it.modeltester.sut.io.FragmentedTlsInput;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;
import se.uu.it.modeltester.sut.io.definitions.Definitions;
import se.uu.it.modeltester.sut.io.definitions.DefinitionsFactory;
import se.uu.it.modeltester.test.FragmentationBug;
import se.uu.it.modeltester.test.FragmentationStrategy;
import se.uu.it.modeltester.test.SpecificationBug;
import se.uu.it.modeltester.test.TestingReport;

public class ModelBasedTester {
	private static final Logger LOGGER = LogManager.getLogger(ModelBasedTester.class);
	private ModelBasedTesterConfig config;

	public ModelBasedTester(ModelBasedTesterConfig config) {
		this.config = config;
	}
	

	public TestingReport startTesting() throws ParseException, IOException {
		SULOracle<TlsInput, TlsOutput> sutOracle = createOracle(config);
		if (config.isOnlyLearn()) {
			extractModel(config);
			return null;
		} else {
			ModelBasedTestingTask task = generateModelBasedTestingTask(config);
			TestingReport report = testModel(sutOracle, task);
			logResult(report, config);
			return report;
		}
	}
	
	private void logResult(TestingReport report, ModelBasedTesterConfig config) throws IOException {
		if (config.getOutput() == null) {
			report.printReport(System.out);
		} else {
			PrintStream ps = new PrintStream(new FileOutputStream(Paths.get(config.getOutput(), "testReport.txt").toFile()));
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
	public ModelBasedTestingTask generateModelBasedTestingTask(ModelBasedTesterConfig config) throws FileNotFoundException, ParseException {
		
		String specification = config.getSpecification(); 
		if (specification == null) {
			ExtractorResult result = extractModel(config);
			// TODO not the nices way of making a conversion
			MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(new TlsProcessor(result.getLearnedModel().generateDefinitions()));
			FastMealy<TlsInput, TlsOutput> fastMealy = dotParser.parseAutomaton(result.getLearnedModelFile().getPath()).get(0);
			return new ModelBasedTestingTask( fastMealy, fastMealy.getInputAlphabet());
		} else {
			Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
			Definitions definitions = DefinitionsFactory.generateDefinitions(alphabet);
			MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(new TlsProcessor(definitions));
			FastMealy<TlsInput, TlsOutput>  model = dotParser.parseAutomaton(specification).get(0);
			return new ModelBasedTestingTask(model, alphabet);
		}
	}
	
	private ExtractorResult extractModel(ModelBasedTesterConfig config) {
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
		Extractor extractor = new Extractor(config, alphabet);
		ExtractorResult result = extractor.extractStateMachine();
		return result;
	}
	
	private TestingReport testModel(SULOracle<TlsInput, TlsOutput> tlsOracle, ModelBasedTestingTask task) {
		LOGGER.info("Starting conformance testing");
		TestingReport report = new TestingReport();
		FastMealy<TlsInput, TlsOutput> model = task.getSpecification();
		Alphabet<TlsInput> inputs = task.getAlphabet();

		List<Word<TlsInput>> stateCover = Automata.stateCover(model, inputs);

		int testNumber = 0;
		int initNumFrag = 2;
		int maxNumFrags = 3;
		
		for (Word<TlsInput> statePrefix : stateCover) {
			FastMealyState<TlsOutput> state = model.getState(statePrefix);
			List<Word<TlsInput>> charSuffixes = Automata.stateCharacterizingSet(model, inputs, state);
			if (charSuffixes.isEmpty())
				charSuffixes = Collections.singletonList(Word.<TlsInput>epsilon());
			for (TlsInput input : inputs) {
				for (Word<TlsInput> suffix : charSuffixes) {
					if (config.getBound() != null && testNumber >= config.getBound()) {
						return report;
					}
					testNumber ++;
					
					Word<TlsInput> regularWord = new WordBuilder<TlsInput>()
							.append(statePrefix)
							.append(input)
							.append(suffix)
							.toWord();
					Word<TlsOutput> regularOutput = tlsOracle.answerQuery(regularWord);
					
					Word<TlsOutput> specificationOutput = model.computeOutput(regularWord);
					
					if (!specificationOutput.equals(regularOutput)) {
						SpecificationBug bug = new SpecificationBug(state, statePrefix, regularWord, specificationOutput, regularOutput);
						report.addItem(bug);
						if (!config.isExhaustive())
							break;
						continue;
					}
					
					boolean bugsFound = false;
//					for (int numFrags=initNumFrag; numFrags<maxNumFrags; numFrags ++) {
//						TlsInput fuzzedInput = new FragmentedTlsInput(input, initNumFrag, FragmentationStrategy.EVEN);
//						Word<TlsInput> fuzzedWord = new WordBuilder<TlsInput>()
//								.append(statePrefix)
//								.append(fuzzedInput)
//								.append(suffix)
//								.toWord();
//						Word<TlsOutput> fuzzedOutput = tlsOracle.answerQuery(fuzzedWord);
//						
//						if (!fuzzedOutput.equals(regularOutput)) {
//							FragmentationBug bug = new FragmentationBug(state, statePrefix, fuzzedWord, regularOutput, fuzzedOutput);
//							report.addItem(bug);
//							bugsFound = true;
//							break;
//						}
//					}
					
					if (bugsFound && !config.isExhaustive()) {
						break;
					}
				}
			}
		}
		
		return report;
	}
}
