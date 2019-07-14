package se.uu.it.dtlsfuzzer.mutate;

import se.uu.it.dtlsfuzzer.execute.PackingResult;

public abstract class PackingMutator implements Mutator<PackingResult> {

	public MutatorType getType() {
		return MutatorType.PACKING;
	}
}
