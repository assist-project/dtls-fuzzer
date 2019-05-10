package se.uu.it.modeltester.test;

import net.automatalib.words.Word;
import se.uu.it.modeltester.mutate.MutatedTlsInput;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class FragmentationBug extends Bug{
	private final Object state;
	private final Word<TlsInput> accessSequence;
	private final Word<TlsInput> inputs;
	private final Word<TlsOutput> expected;
	private final Word<TlsOutput> actual;
	
	public FragmentationBug(Object state, Word<TlsInput> accessSequence, Word<TlsInput> inputs, Word<TlsOutput> expected, Word<TlsOutput> actual) {
		this.state = state;
		this.accessSequence = accessSequence;
		this.inputs = inputs;
		this.expected = expected;
		this.actual = actual;
	}
	
	public MutatedTlsInput getFragmentedInput() {
		return (MutatedTlsInput) inputs.getSymbol(accessSequence.size());
	}
}
