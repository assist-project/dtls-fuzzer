package se.uu.it.dtlsfuzzer.mutate;

import se.uu.it.dtlsfuzzer.execute.FragmentationResult;

public abstract class FragmentationMutator
		implements
			Mutator<FragmentationResult> {

	public MutatorType getType() {
		return MutatorType.FRAGMENTATION;
	}
}
