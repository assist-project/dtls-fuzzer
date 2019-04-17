package de.rub.nds.modelfuzzer;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;

public class ModelBasedTestingTask {
	private MealyMachine<?, TlsInput, ?, TlsOutput>  specification;
	private Alphabet<TlsInput> alphabet;
	public ModelBasedTestingTask(MealyMachine<?, TlsInput, ?, TlsOutput>  specification, Alphabet<TlsInput> alphabet) {
		super();
		this.specification = specification;
		this.alphabet = alphabet;
	}
	
	public MealyMachine<?, TlsInput, ?, TlsOutput>  getSpecification() {
		return specification;
	}
	public Alphabet<TlsInput> getAlphabet() {
		return alphabet;
	}
	
}
