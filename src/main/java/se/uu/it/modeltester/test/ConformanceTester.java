package se.uu.it.modeltester.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.oracles.SULOracle;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import se.uu.it.modeltester.ConformanceTestingTask;
import se.uu.it.modeltester.config.TestingConfig;
import se.uu.it.modeltester.mutate.SplittingMutator;
import se.uu.it.modeltester.mutate.FragmentationGenerator;
import se.uu.it.modeltester.mutate.FragmentationGeneratorFactory;
import se.uu.it.modeltester.mutate.FragmentationStrategy;
import se.uu.it.modeltester.mutate.MutatedTlsInput;
import se.uu.it.modeltester.mutate.RandomSwapMutator;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class ConformanceTester {
	private static final Logger LOGGER = LogManager.getLogger(ConformanceTester.class);
	private TestingConfig config;
	
	public ConformanceTester(TestingConfig config) {
		this.config = config;
	}
	
	/**
	 * Tests conformance of the SUT to the specification. In doing so, it also also 
	 * checks that valid mutations of inputs (fragmentation) prompt the desired behavior
	 * on the SUT.  
	 */
	public TestingReport testModel(SULOracle<TlsInput, TlsOutput> tlsOracle, ConformanceTestingTask task) {
		LOGGER.info("Starting conformance testing");
		TestingReport report = new TestingReport();
		FastMealy<TlsInput, TlsOutput> model = task.getSpecification();
		Alphabet<TlsInput> inputs = task.getAlphabet();

		List<Word<TlsInput>> stateCover = Automata.stateCover(model, inputs);

		int testNumber = 0;
		int initNumFrag = config.getFromNumFrag();
		int maxNumFrags = config.getToNumFrag();
		
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
						report.addBug(bug);
						if (!config.isExhaustive())
							break;
						continue;
					}
					
					boolean bugsFound = false;
					
					for (boolean doShuffling : Arrays.asList(false, true)) { 
						for (FragmentationStrategy strategy : new FragmentationStrategy [] {FragmentationStrategy.EVEN, FragmentationStrategy.RANDOM}) {
							for (int numFrags=initNumFrag; numFrags<maxNumFrags; numFrags ++) {
								TlsInput fuzzedInput = fragment(input, numFrags, strategy, doShuffling);
								Word<TlsInput> fuzzedWord = new WordBuilder<TlsInput>()
										.append(statePrefix)
										.append(fuzzedInput)
										.append(suffix)
										.toWord();
								Word<TlsOutput> fuzzedOutput = tlsOracle.answerQuery(fuzzedWord);
								
								if (!fuzzedOutput.equals(regularOutput)) {
									FragmentationBug bug = new FragmentationBug(state, statePrefix, fuzzedWord, regularOutput, fuzzedOutput);
									report.addBug(bug);
									bugsFound = true;
									break;
								}
							}
							if (bugsFound)
								break;
						}
						if (bugsFound)
							break;
					}
					
					if (bugsFound && !config.isExhaustive()) {
						break;
					}
				}
			}
		}
		
		return report;
	}
	
	private static TlsInput fragment(TlsInput input, int frags, FragmentationStrategy strategy, boolean doShuffling) {
		MutatedTlsInput mutatedInput = new MutatedTlsInput(input);
		SplittingMutator fragmentationMutator = new SplittingMutator(strategy, frags);
		mutatedInput.addMutator(fragmentationMutator);
		if (doShuffling) {
			mutatedInput.addMutator(new RandomSwapMutator(0));
		}
		return mutatedInput;
	}
}
