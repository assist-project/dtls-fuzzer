package de.rub.nds.modelfuzzer.fuzz;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import net.automatalib.words.Word;

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
	
	
}
