package de.rub.nds.modelfuzzer.fuzz;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import net.automatalib.words.Word;

public class SpecificationBug extends Bug{
	
	private Object state;
	private Word<TlsInput> inputs;
	private Word<String> expected;
	private Word<String> actual;

	public SpecificationBug(Object state, Word<TlsInput> accessSequence, Word<TlsInput> inputs, Word<String> expected, Word<String> actual) {
		this.state = state;
		this.inputs = inputs;
		this.expected = expected;
		this.actual = actual;
	} 
}
