package se.uu.it.dtlsfuzzer.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.oracles.CounterOracle;
import de.learnlib.oracles.CounterOracle.MealyCounterOracle;
import de.learnlib.oracles.SULOracle;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import se.uu.it.dtlsfuzzer.ConformanceTestingTask;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;
import se.uu.it.dtlsfuzzer.config.TestingConfig;
import se.uu.it.dtlsfuzzer.mutate.FragmentationMutator;
import se.uu.it.dtlsfuzzer.mutate.FragmentingTlsInput;
import se.uu.it.dtlsfuzzer.mutate.fragment.EmptyFragmentAdditionMutator;
import se.uu.it.dtlsfuzzer.mutate.fragment.FragmentOverlapMutator;
import se.uu.it.dtlsfuzzer.mutate.fragment.FragmentationStrategy;
import se.uu.it.dtlsfuzzer.mutate.fragment.RandomSwapMutator;
import se.uu.it.dtlsfuzzer.mutate.fragment.SplittingMutator;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsInputType;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

public class ConformanceTester {
	private static final Logger LOGGER = LogManager
			.getLogger(ConformanceTester.class);
	public static final String TEST_REPORT_FILENAME = "testReport.txt";
	public static final String TESTS_DIRNAME = "tests";

	private TestingConfig config;
	private String output;

	public ConformanceTester(DtlsFuzzerConfig config) {
		this.config = config.getTestingConfig();
		this.output = config.getOutput();
	}

	/**
	 * Tests conformance of the SUT to the specification. In doing so, it also
	 * also checks that valid mutations of inputs (fragmentation) prompt the
	 * desired behavior on the SUT.
	 * 
	 * @throws IOException
	 */
	public TestReport testModel(SULOracle<TlsInput, TlsOutput> tlsOracle,
			ConformanceTestingTask task) throws IOException {
		LOGGER.info("Starting conformance testing");
		TestReport report = doModelBasedTesting(tlsOracle, task);
		logResult(report);
		return report;
	}

	private TestReport doModelBasedTesting(
			SULOracle<TlsInput, TlsOutput> tlsOracle,
			final ConformanceTestingTask task) {
		TestReport report = new TestReport();
		final MealyCounterOracle<TlsInput, TlsOutput> testOracle = new CounterOracle.MealyCounterOracle<>(
				tlsOracle, "tests");
		FastMealy<TlsInput, TlsOutput> model = task.getSpecification();
		Alphabet<TlsInput> inputs = task.getAlphabet();
		Supplier<Boolean> shouldStop = () -> config.getBound() != null
				&& config.getBound() < testOracle.getCount();

		List<Word<TlsInput>> stateCover = Automata.stateCover(model, inputs);

		for (Word<TlsInput> statePrefix : stateCover) {
			FastMealyState<TlsOutput> state = model.getState(statePrefix);
			for (TlsInput input : inputs) {
				List<Word<TlsInput>> suffixes = generateSuffixes(statePrefix,
						input, inputs, model);
				for (Word<TlsInput> suffix : suffixes) {
					if (shouldStop.get()) {
						return report;
					}

					Word<TlsInput> regularWord = new WordBuilder<TlsInput>()
							.append(statePrefix).append(input).append(suffix)
							.toWord();

					Word<TlsOutput> regularOutput = testOracle
							.answerQuery(regularWord);

					Word<TlsOutput> specificationOutput = model
							.computeOutput(regularWord);

					if (!specificationOutput.equals(regularOutput)) {
						SpecificationBug bug = new SpecificationBug(state,
								statePrefix, regularWord, specificationOutput,
								regularOutput);
						report.addBug(bug);
						if (!config.isExhaustive())
							break;
						continue;
					}

					boolean bugsFound = false;

					if (input.getInputType() == TlsInputType.HANDSHAKE) {
						List<FragmentingTlsInput> fragmentingInputs = generateFragmentingInputs(input);
						for (FragmentingTlsInput fragmentingInput : fragmentingInputs) {
							Word<TlsInput> fragmentingWord = new WordBuilder<TlsInput>()
									.append(statePrefix)
									.append(fragmentingInput).append(suffix)
									.toWord();
							Word<TlsOutput> fuzzedOutput = testOracle
									.answerQuery(fragmentingWord);

							if (!fuzzedOutput.equals(regularOutput)) {
								FragmentationBug bug = new FragmentationBug(
										state, statePrefix, fragmentingInput,
										suffix, regularOutput, fuzzedOutput);
								report.addBug(bug);
								bugsFound = true;
								if (fragmentingInputs.indexOf(fragmentingInput) == 0)
									break;
							}

							if (shouldStop.get()) {
								return report;
							}
						}

						if (bugsFound && !config.isExhaustive()) {
							break;
						}
					}
				}
			}
		}

		return report;
	}

	private List<Word<TlsInput>> generateSuffixes(Word<TlsInput> statePrefix,
			TlsInput input, Alphabet<TlsInput> inputs,
			FastMealy<TlsInput, TlsOutput> model) {
		FastMealyState<TlsOutput> nextState = model.getState(statePrefix
				.append(input));
		List<Word<TlsInput>> charSuffixes = Automata.stateCharacterizingSet(
				model, inputs, nextState);
		Stream<Word<TlsInput>> stream = charSuffixes.stream();
		for (int i = 0; i < config.getMidLength(); i++) {
			stream = stream.flatMap(word -> inputs.stream().map(
					inp -> Word.fromSymbols(inp).concat(word)));
		}

		return stream.distinct().collect(Collectors.toList());
	}

	private List<FragmentingTlsInput> generateFragmentingInputs(TlsInput input) {
		List<FragmentingTlsInput> fragmentingInputs = new ArrayList<>();

		for (boolean addOverlap : Arrays.asList(false, true))
			for (boolean doShuffling : Arrays.asList(false, true))
				for (FragmentationStrategy strategy : new FragmentationStrategy[]{
						FragmentationStrategy.EVEN,
						FragmentationStrategy.RANDOM})
					for (int numFrags = config.getFromNumFrag(); numFrags < config
							.getToNumFrag(); numFrags++) {
						FragmentingTlsInput fragmentingInput = fragment(input,
								numFrags, strategy, false, doShuffling,
								addOverlap);
						fragmentingInputs.add(fragmentingInput);
					}

		// empty fragment test
		fragmentingInputs.add(fragment(input, 2, FragmentationStrategy.EVEN,
				true, false, false));

		return fragmentingInputs;
	}

	private FragmentingTlsInput fragment(TlsInput input, int frags,
			FragmentationStrategy strategy, boolean addEmptyFragment,
			boolean doShuffling, boolean addOverlap) {
		List<FragmentationMutator> mutators = new ArrayList<>();
		if (frags > 1) {
			SplittingMutator fragmentationMutator = new SplittingMutator(
					strategy, frags);
			mutators.add(fragmentationMutator);
		}
		if (addEmptyFragment) {
			mutators.add(new EmptyFragmentAdditionMutator(config.getTestSeed()));
		}
		if (doShuffling) {
			mutators.add(new RandomSwapMutator(config.getTestSeed()));
		}
		if (addOverlap && frags > 1) {
			mutators.add(new FragmentOverlapMutator(config.getTestSeed()));
		}
		return new FragmentingTlsInput(input, mutators);
	}

	private void logResult(TestReport report) throws IOException {
		if (output == null) {
			report.printReport(System.out);
		} else {
			File testReport = Paths.get(output, TEST_REPORT_FILENAME).toFile();
			testReport.createNewFile();
			PrintStream ps = new PrintStream(new FileOutputStream(testReport));
			report.printReport(ps);
			if (config.isTestGenerationEnabled()) {
				File testsDirectory = Paths.get(output, TESTS_DIRNAME).toFile();
				testsDirectory.mkdir();
				se.uu.it.dtlsfuzzer.TestParser parser = new se.uu.it.dtlsfuzzer.TestParser();
				for (Bug bug : report.getBugs()) {
					parser.writeTest(
							bug.getReproducingTest(),
							Paths.get(output, TESTS_DIRNAME,
									"Bug" + bug.getBugId()).toString());
				}
			}
		}
	}

}
