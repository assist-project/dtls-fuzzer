package de.rub.nds.modelfuzzer.learn;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.SUL;
import de.learnlib.oracles.SULOracle;
import de.rub.nds.modelfuzzer.config.ModelBasedTesterConfig;
import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.tlsattacker.attacks.util.response.ResponseFingerprint;
import java.util.Random;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class EquivalenceAlgorithmFactory {

	private EquivalenceAlgorithmFactory() {
	}

//	public static EquivalenceOracle getAlgorithm(
//			EquivalenceTestAlgorithmName name, Alphabet alphabet,
//			SUL systemUnderTest, ModelBasedFuzzerConfig config) {
//		SULOracle<TlsInput, Word<ResponseFingerprint>> sulOracle = new SULOracle<>(
//				systemUnderTest);
//		return getAlgorithm(name, alphabet, sulOracle, systemUnderTest, config);
//	}
//
//	public static EquivalenceOracle getAlgorithm(
//			EquivalenceTestAlgorithmName name, Alphabet alphabet,
//			MembershipOracle sulOracle, SUL systemUnderTest,
//			VulnerabilityFinderConfig config) {
//		switch (name) {
//			case W_METHOD :
//				return new WMethodEQOracle(config.getMaxDepth(), sulOracle);
//			case RANDOM_WORDS :
//				return new RandomWordsEQOracle(sulOracle,
//						config.getMinLength(), config.getMaxLength(),
//						config.getNumberOfQueries(), new Random());
//			case RANDOM_WALK :
//				return new RandomWalkEQOracle(0, config.getMaxLength(),
//						new Random(), systemUnderTest);
//			case WP_METHOD :
//				return new WpMethodEQOracle(config.getMaxDepth(), sulOracle);
//			case RANDOM_WP_METHOD :
//				return new RandomWpMethodEQOracle(sulOracle,
//						config.getMinLength(), 5, config.getNumberOfQueries());
//			default :
//				throw new UnsupportedOperationException(
//						"Unknown EquivalenceTesting Algorithm");// TODO change
//																// exception
//																// type
//		}
//	}
}
