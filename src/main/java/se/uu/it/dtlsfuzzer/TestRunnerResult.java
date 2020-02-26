package se.uu.it.dtlsfuzzer;

import java.util.Map;

import net.automatalib.words.Word;

/**
 * Result for a single test comprising the sequence of inputs and a summary of
 * the sequences of outputs generated.
 */
public class TestRunnerResult<I, O> {
	private Word<I> inputWord;
	private Map<Word<O>, Integer> generatedOutputs;

	public TestRunnerResult(Word<I> inputWord,
			Map<Word<O>, Integer> generatedOutputs) {
		super();
		this.inputWord = inputWord;
		this.generatedOutputs = generatedOutputs;
	}

	public Word<I> getInputWord() {
		return inputWord;
	}

	public Map<Word<O>, Integer> getGeneratedOutputs() {
		return generatedOutputs;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Test: ").append(inputWord).append("\n");
		for (Word<O> answer : generatedOutputs.keySet()) {
			sb.append(generatedOutputs.get(answer)).append(
					" times outputs: " + answer.toString() + "\n");
		}

		return sb.toString();
	}
}
