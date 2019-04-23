package se.uu.it.modeltester.mutate;

import se.uu.it.modeltester.execute.FragmentationResult;

public abstract class FragmentationMutator implements Mutator<FragmentationResult>{
	
	public MutatorType getType() {
		return MutatorType.FRAGMENTATION;
	}
}
