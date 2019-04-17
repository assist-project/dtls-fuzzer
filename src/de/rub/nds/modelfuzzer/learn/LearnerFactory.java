package de.rub.nds.modelfuzzer.learn;


import java.util.Random;

import com.google.common.collect.Lists;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.counterexamples.AcexLocalSuffixFinder;
import de.learnlib.api.SUL;
import de.learnlib.eqtests.basic.WMethodEQOracle;
import de.learnlib.eqtests.basic.WpMethodEQOracle;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.rub.nds.modelfuzzer.config.LearningConfig;
import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class LearnerFactory {
	public static MealyLearner<TlsInput,TlsOutput> loadLearner(
			LearningConfig config, MealyMembershipOracle<TlsInput,TlsOutput> sulOracle, 
			Alphabet<TlsInput> alphabet) {
		switch (config.getLearningAlgorithm()){
			case LSTAR:
				return new ExtensibleLStarMealy<TlsInput, TlsOutput>(alphabet, sulOracle, 
						Lists.<Word<TlsInput>>newArrayList(), ObservationTableCEXHandlers.CLASSIC_LSTAR, ClosingStrategies.CLOSE_SHORTEST);
			case RS:
				return new ExtensibleLStarMealy<TlsInput, TlsOutput>(alphabet, sulOracle, 
						Lists.<Word<TlsInput>>newArrayList(), ObservationTableCEXHandlers.RIVEST_SCHAPIRE, ClosingStrategies.CLOSE_SHORTEST);
			case TTT:
				AcexLocalSuffixFinder suffixFinder = new AcexLocalSuffixFinder(
						AcexAnalyzers.BINARY_SEARCH, false, "Analyzer");
				return new TTTLearnerMealy<TlsInput, TlsOutput>(alphabet, sulOracle, suffixFinder);
			case KV:
				return new KearnsVaziraniMealy<TlsInput, TlsOutput>(alphabet, sulOracle, false, AcexAnalyzers.LINEAR_FWD);
			default:
				throw new RuntimeException("Learner algorithm not supported");
		}
	}

	
//	W_METHOD, MODIFIED_W_METHOD, WP_METHOD, RANDOM_WORDS, RANDOM_WALK, RANDOM_WP_METHOD 
	public static EquivalenceOracle<MealyMachine<?, TlsInput, ?, TlsOutput>, TlsInput, Word<TlsOutput>> loadTester(
			LearningConfig config, SUL<TlsInput,TlsOutput> sul, MealyMembershipOracle<TlsInput,TlsOutput> sulOracle) {
		switch (config.getEquivalenceAlgorithm()){
			// simplest method, but doesn't perform well in practice, especially for large models
			case RANDOM_WALK:
				return new RandomWalkEQOracle<TlsInput, TlsOutput>(config.getProbReset(), config.getNumberOfQueries(), true, new Random(123456l), sul);
			// Other methods are somewhat smarter than random testing: state coverage, trying to distinguish states, etc.
			case W_METHOD:
				return new WMethodEQOracle.MealyWMethodEQOracle<>(config.getMaxDepth(), sulOracle);
			case WP_METHOD:
				return new WpMethodEQOracle.MealyWpMethodEQOracle<>(config.getMaxDepth(), sulOracle);
			case RANDOM_WP_METHOD:
				return new RandomWpMethodEQOracle<>(sulOracle, config.getMinLength(), config.getRandLength(), config.getNumberOfQueries());
			default:
				throw new RuntimeException("Equivalence algorithm not supported");
		}
	}


}
