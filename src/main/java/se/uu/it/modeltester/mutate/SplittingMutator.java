package se.uu.it.modeltester.mutate;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.FragmentationResult;

public class SplittingMutator extends FragmentationMutator{
	private FragmentationGenerator generator;
	private FragmentationStrategy strategy;
	private int numFragments;

	public SplittingMutator(FragmentationStrategy strategy, int numFragments) {
		this.numFragments = numFragments;
		this.strategy = strategy;
		this.generator = FragmentationGeneratorFactory.buildGenerator(strategy);
	}
	
	@Override
	public Mutation<FragmentationResult> generateMutation(FragmentationResult result, TlsContext context) {
		int length = result.getMessage().getLength().getValue();
		Fragmentation fragmentation = generator.generateFragmentation(numFragments, length);
		return new FragmentationMutation(fragmentation);
	}
	
	public String toString() {
		return "SplittingMutator(" + strategy.name() + "," + numFragments + ")";
	}

}
