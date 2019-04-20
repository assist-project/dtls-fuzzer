package se.uu.it.modeltester.execute.mutate;

import se.uu.it.modeltester.execute.PackingResult;

public abstract class PackingMutator implements Mutator<PackingResult>{

	public MutatorType getType() {
		return MutatorType.PACKING;
	}
}
