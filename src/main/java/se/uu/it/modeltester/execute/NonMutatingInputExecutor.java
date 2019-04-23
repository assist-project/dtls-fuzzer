package se.uu.it.modeltester.execute;

import se.uu.it.modeltester.mutate.Mutator;

/**
 * Builds on the MutatingInputExecutor, but applies no mutation.
 * 
 */
public final class NonMutatingInputExecutor extends MutatingInputExecutor{

	public final <M extends Mutator<?>> boolean addMutator(M mutator) {
		return false;
	}
}
