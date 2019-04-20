package se.uu.it.modeltester.execute.mutate;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.FragmentationResult;

public abstract class FragmentationMutator implements Mutator<FragmentationResult>{
	/**
	 * Mutates the fragmentation result (by for example, replacing a strategy of fragmenting by another).
	 */
	public abstract FragmentationResult mutate(FragmentationResult result, TlsContext context);
	
	public MutatorType getType() {
		return MutatorType.FRAGMENTATION;
	}
}
