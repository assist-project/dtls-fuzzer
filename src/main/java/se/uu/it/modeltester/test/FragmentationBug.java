package se.uu.it.modeltester.test;

import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.words.Word;
import se.uu.it.modeltester.mutate.MutatingTlsInput;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class FragmentationBug extends Bug{
	private final FastMealyState<TlsOutput> state;
	private final Word<TlsInput> accessSequence;
	private final Word<TlsInput> inputs;
	private final Word<TlsOutput> expected;
	private final Word<TlsOutput> actual;
	
	public FragmentationBug(FastMealyState<TlsOutput> state, Word<TlsInput> accessSequence,
			Word<TlsInput> inputs, 
			Word<TlsOutput> expected, 
			Word<TlsOutput> actual) {
		super(BugType.FRAGMENTATION, inputs, expected, actual);
		this.state = state;
		this.accessSequence = accessSequence;
		this.inputs = inputs;
		this.expected = expected;
		this.actual = actual;
		
	}
	
	public FastMealyState<TlsOutput> getState() {
		return state;
	}
	
	public MutatingTlsInput getFragmentedInput() {
		return (MutatingTlsInput) inputs.getSymbol(accessSequence.size());
	}
}
