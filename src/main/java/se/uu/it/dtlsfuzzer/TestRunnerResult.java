package se.uu.it.dtlsfuzzer;

import java.util.Map;

import net.automatalib.words.Word;

/**
 * Result for a single test
 */
public class TestRunnerResult<I, O> {
	private Word<I> inputWord;
	private Map<Word<O>, Integer> generatedOutputs;
	private Word<O> expectedOutputWord;

	public TestRunnerResult(Word<I> inputWord,
			Map<Word<O>, Integer> generatedOutputs) {
		super();
		this.inputWord = inputWord;
		this.generatedOutputs = generatedOutputs;
	}

	public void setExpectedOutputWord(Word<O> outputWord) {
		expectedOutputWord = outputWord;
	}

	public Word<I> getInputWord() {
		return inputWord;
	}

	public Map<Word<O>, Integer> getGeneratedOutputs() {
		return generatedOutputs;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Test: ").append(inputWord).append(System.lineSeparator());
		for (Word<O> answer : generatedOutputs.keySet()) {
			sb.append(generatedOutputs.get(answer)).append(
					" times outputs: " + answer.toString() + System.lineSeparator());
		}

		if (expectedOutputWord != null) {
			sb.append("Expected output: " + expectedOutputWord);
		}

		return sb.toString();
	}
}
