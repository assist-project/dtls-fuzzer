package se.uu.it.dtlsfuzzer;

import java.util.Map;

import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

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
}
