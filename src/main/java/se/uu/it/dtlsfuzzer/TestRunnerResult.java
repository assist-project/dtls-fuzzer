package se.uu.it.dtlsfuzzer;

import java.util.Map;

import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

/**
 * Result for a single test
 */
public class TestRunnerResult {
	private Word<TlsInput> inputWord;
	private Map<Word<TlsOutput>, Integer> generatedOutputs;

	public TestRunnerResult(Word<TlsInput> inputWord,
			Map<Word<TlsOutput>, Integer> generatedOutputs) {
		super();
		this.inputWord = inputWord;
		this.generatedOutputs = generatedOutputs;
	}

	public Word<TlsInput> getInputWord() {
		return inputWord;
	}

	public Map<Word<TlsOutput>, Integer> getGeneratedOutputs() {
		return generatedOutputs;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Inputs: ").append(inputWord).append("\n");
		for (Word<TlsOutput> answer : generatedOutputs.keySet()) {
			sb.append(generatedOutputs.get(answer)).append(
					" times outputs: " + answer.toString() + "\n");
		}

		return sb.toString();
	}
}
