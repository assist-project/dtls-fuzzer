package se.uu.it.dtlsfuzzer.learner;

import com.google.common.collect.Lists;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.MealyEQOracleChain;
import de.learnlib.oracle.equivalence.MealyWMethodEQOracle;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.TestParser;
import se.uu.it.dtlsfuzzer.config.LearningConfig;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class LearnerFactory {
	public static final long SEED = 123456l;

	public static MealyLearner<TlsInput, TlsOutput> loadLearner(
			LearningConfig config,
			MealyMembershipOracle<TlsInput, TlsOutput> sulOracle,
			Alphabet<TlsInput> alphabet) {
		switch (config.getLearningAlgorithm()) {
			case LSTAR :
				return new ExtensibleLStarMealy<TlsInput, TlsOutput>(alphabet,
						sulOracle, Lists.<Word<TlsInput>> newArrayList(),
						ObservationTableCEXHandlers.CLASSIC_LSTAR,
						ClosingStrategies.CLOSE_SHORTEST);
			case RS :
				return new ExtensibleLStarMealy<TlsInput, TlsOutput>(alphabet,
						sulOracle, Lists.<Word<TlsInput>> newArrayList(),
						ObservationTableCEXHandlers.RIVEST_SCHAPIRE,
						ClosingStrategies.CLOSE_SHORTEST);
			case TTT :
				return new TTTLearnerMealyBuilder<TlsInput, TlsOutput>()
						.withAlphabet(alphabet).withOracle(sulOracle)
						.withAnalyzer(AcexAnalyzers.BINARY_SEARCH_FWD).create();
			case KV :
				return new KearnsVaziraniMealy<TlsInput, TlsOutput>(alphabet,
						sulOracle, false, AcexAnalyzers.LINEAR_FWD);
			default :
				throw new RuntimeException("Learner algorithm not supported");
		}
	}

	// W_METHOD, MODIFIED_W_METHOD, WP_METHOD, RANDOM_WORDS, RANDOM_WALK,
	// RANDOM_WP_METHOD
	public static EquivalenceOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, Word<TlsOutput>> loadTester(
			LearningConfig config, SUL<TlsInput, TlsOutput> sul,
			MealyMembershipOracle<TlsInput, TlsOutput> sulOracle,
			Alphabet<TlsInput> alphabet) {
		if (config.getEquivalenceAlgorithms().isEmpty()) {
			return (m, i) -> null;
		} else {
			if (config.getEquivalenceAlgorithms().size() == 1) {
				return loadTesterForAlgorithm(config.getEquivalenceAlgorithms()
						.get(0), config, sul, sulOracle, alphabet);
			} else {
				List<EquivalenceOracle.MealyEquivalenceOracle<TlsInput, TlsOutput>> eqOracles = config
						.getEquivalenceAlgorithms()
						.stream()
						.map(alg -> loadTesterForAlgorithm(alg, config, sul,
								sulOracle, alphabet))
						.collect(Collectors.toList());
				MealyEQOracleChain<TlsInput, TlsOutput> eqChain = new MealyEQOracleChain<TlsInput, TlsOutput>(
						eqOracles);
				return eqChain;
			}
		}
	}

	private static EquivalenceOracle.MealyEquivalenceOracle<TlsInput, TlsOutput> loadTesterForAlgorithm(
			EquivalenceAlgorithmName algorithm, LearningConfig config,
			SUL<TlsInput, TlsOutput> sul,
			MealyMembershipOracle<TlsInput, TlsOutput> sulOracle,
			Alphabet<TlsInput> alphabet) {
		// simplest method, but doesn't perform well in practice, especially for
		// large models
		switch (algorithm) {
			case RANDOM_WALK :
				return new RandomWalkEQOracle<TlsInput, TlsOutput>(sul,
						config.getProbReset(), config.getNumberOfQueries(),
						true, new Random(config.getSeed()));
				// Other methods are somewhat smarter than random testing: state
				// coverage, trying to distinguish states, etc.
			case W_METHOD :
				return new MealyWMethodEQOracle<>(sulOracle,
						config.getMaxDepth());
			case WP_METHOD :
				return new MealyWpMethodEQOracle<>(sulOracle,
						config.getMaxDepth());
			case RANDOM_WP_METHOD :
				return new RandomWpMethodEQOracle<>(sulOracle,
						config.getMinLength(), config.getRandLength(),
						config.getNumberOfQueries(), config.getSeed());
			case SAMPLED_TESTS :
				return new SampledTestsEQOracle<>(readTests(config, alphabet),
						sulOracle);
			case WP_SAMPLED_TESTS :
				return new WpSampledTestsEQOracle<>(
						readTests(config, alphabet), sulOracle,
						config.getMinLength(), config.getRandLength(),
						config.getSeed(), config.getNumberOfQueries());
			default :
				throw new RuntimeException(
						"Equivalence algorithm not supported");
		}
	}

	private static List<Word<TlsInput>> readTests(LearningConfig config,
			Alphabet<TlsInput> alphabet) {
		TestParser parser = new TestParser();
		try {
			List<Word<TlsInput>> tests = parser.readTests(alphabet,
					config.getTestFile());
			return tests;
		} catch (IOException e) {
			throw new RuntimeException("Could not read tests from file "
					+ config.getTestFile(), e);
		}
	}
}
