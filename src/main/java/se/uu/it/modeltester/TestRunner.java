package se.uu.it.modeltester;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.oracles.SULOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.modeltester.config.TestRunnerConfig;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class TestRunner {
	private static final Logger LOGGER = LogManager.getLogger(TestRunner.class);
	private SULOracle<TlsInput, TlsOutput> sulOracle;
	private TestRunnerConfig config;
	private Alphabet<TlsInput> alphabet;

	public TestRunner(TestRunnerConfig config, Alphabet<TlsInput> alphabet,
			SULOracle<TlsInput, TlsOutput> sutOracle) {
		this.alphabet = alphabet;
		this.config = config;
		this.sulOracle = sutOracle;
	}

	public TestRunnerResult runTest() throws IOException {
		TestParser testParser = new TestParser();
		Word<TlsInput> test = testParser.readTest(alphabet, config.getTest());

		Map<Word<TlsOutput>, Integer> answerMap = new LinkedHashMap<>();
		for (int i = 0; i < config.getTimes(); i++) {
			Word<TlsOutput> answer = sulOracle.answerQuery(test);
			if (!answerMap.containsKey(answer)) {
				answerMap.put(answer, 1);
			} else {
				answerMap.put(answer, answerMap.get(answer) + 1);
			}
		}
		LOGGER.error("Inputs: " + test + "\n");
		for (Word<TlsOutput> answer : answerMap.keySet()) {
			LOGGER.error(answerMap.get(answer) + " times outputs: "
					+ answer.toString() + "\n");
		}

		return new TestRunnerResult(test, answerMap);
	}
}
