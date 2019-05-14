package se.uu.it.modeltester.test;

import net.automatalib.words.Word;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public abstract class Bug extends AutoLoggable{
	private final BugType type;
	private Word<TlsInput> inputs;
	private Word<TlsOutput> expected;
	private Word<TlsOutput> actual;
	
	public Bug(BugType type, Word<TlsInput> inputs, Word<TlsOutput> expected, Word<TlsOutput> actual) {
		this.inputs = inputs;
		this.expected = expected;
		this.actual = actual;
		this.type = type;
	}
	
	public Word<TlsInput> getInputs() {
		return inputs;
	}

	public Word<TlsOutput> getExpected() {
		return expected;
	}

	public Word<TlsOutput> getActual() {
		return actual;
	}

	public BugType getType() {
		return type;
	}
	
	
	public void minimize() {
		Word<TlsOutput> commonPrefix = expected.longestCommonPrefix(actual);
		assert commonPrefix.size() < expected.size() && commonPrefix.size() < actual.size() && expected.size() == actual.size();
		this.expected = commonPrefix.append(expected.getSymbol(commonPrefix.size()));
		this.inputs = inputs.prefix(commonPrefix.size()+1);
		this.actual = commonPrefix.append(actual.getSymbol(commonPrefix.size()));
	}
	
}
