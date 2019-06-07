package se.uu.it.modeltester.test;

import net.automatalib.words.Word;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class SpecificationBug extends Bug {

	private Object state;

	public SpecificationBug(Object state, Word<TlsInput> accessSequence,
			Word<TlsInput> inputs, Word<TlsOutput> expected,
			Word<TlsOutput> actual) {
		super(inputs, expected, actual);
		this.state = state;
	}

	@Override
	public Word<TlsInput> getReproducingTest() {
		return this.getInputs();
	}

	@Override
	public BugType getType() {
		return BugType.SPECIFICATION;
	}
}
