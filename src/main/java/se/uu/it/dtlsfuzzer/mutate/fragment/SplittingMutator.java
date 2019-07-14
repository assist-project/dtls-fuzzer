package se.uu.it.dtlsfuzzer.mutate.fragment;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.execute.FragmentationResult;
import se.uu.it.dtlsfuzzer.mutate.FragmentationMutator;
import se.uu.it.dtlsfuzzer.mutate.Mutation;

public class SplittingMutator extends FragmentationMutator {
	private FragmentationGenerator generator;
	private FragmentationStrategy strategy;
	private int numFragments;

	public SplittingMutator(FragmentationStrategy strategy, int numFragments) {
		this.numFragments = numFragments;
		this.strategy = strategy;
		this.generator = FragmentationGeneratorFactory.buildGenerator(strategy);
	}

	@Override
	public Mutation<FragmentationResult> generateMutation(
			FragmentationResult result, TlsContext context,
			ExecutionContext exContext) {
		int length = result.getMessage().getLength().getValue();
		Fragmentation fragmentation = generator.generateFragmentation(
				numFragments, length);
		return new SplittingMutation(fragmentation);
	}

	public String toString() {
		return "SplittingMutator(" + strategy.name() + "," + numFragments + ")";
	}

}
