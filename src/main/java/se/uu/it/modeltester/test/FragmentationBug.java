package se.uu.it.modeltester.test;

import java.util.List;

import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.words.Word;
import se.uu.it.modeltester.mutate.FragmentingTlsInput;
import se.uu.it.modeltester.mutate.MutatedTlsInput;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class FragmentationBug extends Bug{
	private final FastMealyState<TlsOutput> state;
	private final Word<TlsInput> accessSequence;
	private final FragmentingTlsInput fragmentingInput;
	private MutatedTlsInput fragmentedInput;
	
	public FragmentationBug(FastMealyState<TlsOutput> state, Word<TlsInput> accessSequence,
			FragmentingTlsInput fragmentingInput,
			Word<TlsInput> suffix, 
			Word<TlsOutput> expected, 
			Word<TlsOutput> actual) {
		super(accessSequence.append(fragmentingInput).concat(suffix), expected, actual);
		this.state = state;
		this.accessSequence = accessSequence;
		this.fragmentingInput = fragmentingInput;
		List<Mutation<?>> fragmentationMutations = fragmentingInput.getExecutorAsMutating().getLastAppliedMutations();
		this.fragmentedInput = new MutatedTlsInput(fragmentingInput.getInput(), fragmentationMutations);
	}
	
	public FastMealyState<TlsOutput> getState() {
		return state;
	}
	
	/*
	 * A reproducible test will expose the fragmentation mutations that were generated while executing the input.
	 */
	public Word<TlsInput> getReproducibleTest() {
		return accessSequence.append(fragmentedInput).concat(getInputs().suffix(getInputs().length() - accessSequence.length() - 1));
	}
	
	public FragmentingTlsInput getFragmentingInput() {
		return fragmentingInput;
	}

	@Override
	public BugType getType() {
		return BugType.FRAGMENTATION;
	}
}
