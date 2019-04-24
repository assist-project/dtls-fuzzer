package se.uu.it.modeltester.mutate;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.FragmentationResult;

public class BasicFragmentationMutator extends FragmentationMutator{
	private FragmentationGenerator generator;
	private int numFragments;

	public BasicFragmentationMutator(FragmentationGenerator generator, int numFragments) {
		this.generator = generator;
		this.numFragments = numFragments;
	}
	
	@Override
	public Mutation<FragmentationResult> generateMutation(FragmentationResult result, TlsContext context) {
		int length = result.getMessage().getLength().getValue();
		Fragmentation fragmentation = generator.generateFragmentation(numFragments, length);
		return new FragmentationMutation(fragmentation);
	}

}
