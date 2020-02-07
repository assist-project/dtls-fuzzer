package se.uu.it.dtlsfuzzer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.config.TestRunnerConfig;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

public class TestRunner {
	private static final Logger LOGGER = LogManager.getLogger(TestRunner.class);
	private MealyMembershipOracle<TlsInput, TlsOutput> sulOracle;
	private TestRunnerConfig config;
	private Alphabet<TlsInput> alphabet;

	public TestRunner(TestRunnerConfig config, Alphabet<TlsInput> alphabet,
			MealyMembershipOracle<TlsInput, TlsOutput> sutOracle) {
		this.alphabet = alphabet;
		this.config = config;
		this.sulOracle = sutOracle;
	}

	public List<TestRunnerResult<TlsInput, TlsOutput>> runTests()
			throws IOException {
		TestParser testParser = new TestParser();
		List<Word<TlsInput>> tests = testParser.readTests(alphabet,
				config.getTest());
		List<TestRunnerResult<TlsInput, TlsOutput>> results = tests.stream()
				.map(t -> runTest(t)).collect(Collectors.toList());
		return results;
	}

	private TestRunnerResult<TlsInput, TlsOutput> runTest(Word<TlsInput> test) {
		TestRunnerResult<TlsInput, TlsOutput> result = runTest(test,
				config.getTimes(), sulOracle);
		return result;
	}

	public static <I, O> TestRunnerResult<I, O> runTest(Word<I> test,
			int times, MealyMembershipOracle<I, O> sulOracle) {
		LinkedHashMap<Word<O>, Integer> answerMap = new LinkedHashMap<>();
		for (int i = 0; i < times; i++) {
			Word<O> answer = sulOracle.answerQuery(test);
			if (!answerMap.containsKey(answer)) {
				answerMap.put(answer, 1);
			} else {
				answerMap.put(answer, answerMap.get(answer) + 1);
			}
		}
		return new TestRunnerResult<I, O>(test, answerMap);
	}
}
