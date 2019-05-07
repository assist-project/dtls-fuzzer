package se.uu.it.modeltester;

import java.util.Map;

import net.automatalib.words.Word;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class TraceRunnerResult {
	private Word<TlsInput> inputWord;
	private Map<Word<TlsOutput>, Integer> generatedOutputs;
	
	public TraceRunnerResult(Word<TlsInput> inputWord, Map<Word<TlsOutput>, Integer> generatedOutputs) {
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
