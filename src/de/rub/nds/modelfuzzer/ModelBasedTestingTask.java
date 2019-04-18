package de.rub.nds.modelfuzzer;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.words.Alphabet;

// the only reason we are using FastMealy is to simplify the generics, any Mealy machine implementations
// would do just fine
public class ModelBasedTestingTask {
	private FastMealy<TlsInput, TlsOutput>  specification;
	private Alphabet<TlsInput> alphabet;
	public ModelBasedTestingTask(FastMealy<TlsInput, TlsOutput>  specification, Alphabet<TlsInput> alphabet) {
		super();
		this.specification = specification;
		this.alphabet = alphabet;
	}
	
	public FastMealy<TlsInput, TlsOutput>  getSpecification() {
		return specification;
	}
	public Alphabet<TlsInput> getAlphabet() {
		return alphabet;
	}
	
}
