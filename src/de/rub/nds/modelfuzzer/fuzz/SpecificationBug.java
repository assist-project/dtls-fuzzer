package de.rub.nds.modelfuzzer.fuzz;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import net.automatalib.words.Word;

public class SpecificationBug extends Bug{
	
	private Object state;
	private Word<TlsInput> inputs;
	private Word<TlsOutput> expected;
	private Word<TlsOutput> actual;

	public SpecificationBug(Object state, Word<TlsInput> accessSequence, Word<TlsInput> inputs, Word<TlsOutput> expected, Word<TlsOutput> actual) {
		this.state = state;
		this.inputs = inputs;
		this.expected = expected;
		this.actual = actual;
	} 
}
