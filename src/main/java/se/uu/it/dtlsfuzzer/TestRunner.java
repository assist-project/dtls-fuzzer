package se.uu.it.dtlsfuzzer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.oracles.SULOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.config.TestRunnerConfig;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

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

	public List<TestRunnerResult> runTests() throws IOException {
		TestParser testParser = new TestParser();
		List<Word<TlsInput>> tests = testParser.readTests(alphabet,
				config.getTest());
		List<TestRunnerResult> results = tests.stream().map(t -> runTest(t))
				.collect(Collectors.toList());
		return results;
	}

	private TestRunnerResult runTest(Word<TlsInput> test) {
		Map<Word<TlsOutput>, Integer> answerMap = new LinkedHashMap<>();
		for (int i = 0; i < config.getTimes(); i++) {
			Word<TlsOutput> answer = sulOracle.answerQuery(test);
			if (!answerMap.containsKey(answer)) {
				answerMap.put(answer, 1);
			} else {
				answerMap.put(answer, answerMap.get(answer) + 1);
			}
		}
		return new TestRunnerResult(test, answerMap);
	}
}
