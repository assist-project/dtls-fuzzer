package se.uu.it.modeltester.mutate.fragment;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.mutate.FragmentationMutator;
import se.uu.it.modeltester.mutate.Mutation;

public class FragmentRemovalMutator extends FragmentationMutator {
	
	private Random rand;
	private int numFrags;

	public FragmentRemovalMutator(int seed, int numFrags) {
		this.rand = new Random(seed);
		this.numFrags = numFrags;
	}

	@Override
	public Mutation<FragmentationResult> generateMutation(FragmentationResult result, TlsContext context, ExecutionContext exContext) {
		int size = result.getFragments().size();
		Integer [] mapping = IntStream.range(0, size).boxed().toArray(Integer[]::new);
		List<Integer> indexes = Arrays.asList(mapping);
		int toRemove = Math.min(size, numFrags);
		Integer indexToRemove;
		for (int i=0; i<toRemove; i++) {
			indexToRemove = indexes.remove(rand.nextInt(indexes.size()));
			mapping[indexToRemove] = null;
		}
		ReorderingMutation mutation = new ReorderingMutation(mapping);
		return mutation;
	}

}
