package se.uu.it.modeltester.fuzz;

import net.automatalib.words.Word;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public abstract class ConformanceBug extends Bug{
	private Object state;
	private Word<TlsInput> accessSequence;
	private Word<TlsInput> inputs;
	private Word<TlsOutput> expected;
	private Word<TlsOutput> actual;
	
	public ConformanceBug(Object state, Word<TlsInput> accessSequence, Word<TlsInput> inputs, Word<TlsOutput> expected, Word<TlsOutput> actual) {
		this.state = state;
		this.accessSequence = accessSequence;
		Word<TlsOutput> commonPrefix = expected.longestCommonPrefix(actual);
		assert commonPrefix.size() < expected.size() && commonPrefix.size() < actual.size() && expected.size() == actual.size();
		this.expected = commonPrefix.append(expected.getSymbol(commonPrefix.size()));
		this.inputs = inputs.prefix(commonPrefix.size()+1);
		this.actual = commonPrefix.append(actual.getSymbol(commonPrefix.size()));
	}
	
}
