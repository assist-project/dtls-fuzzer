package se.uu.it.modeltester.mutate.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.mutate.FragmentationMutator;
import se.uu.it.modeltester.mutate.Mutation;

public class FragmentRemovalMutator extends FragmentationMutator {

	public static interface RemovedFragmentSelector {
		public List<Integer> getIndexesOfRemovedFragments(int numFrags);
	}

	public static class PresetSelector {
		private List<Integer> fragsToRemove;
		public PresetSelector(Integer... fragIndexes) {
			fragsToRemove = Arrays.asList(fragIndexes);
		}

		public List<Integer> getIndexesOfRemovedFragments(int numFrags) {
			fragsToRemove.removeIf(idx -> idx >= numFrags);
			return fragsToRemove;
		}
	}

	public static class RandomSelector implements RemovedFragmentSelector {
		private int numFragsToRemove;
		private Random rand;

		public RandomSelector(long seed, int numFragsToRemove) {
			this.numFragsToRemove = numFragsToRemove;
			rand = new Random(seed);
		}

		@Override
		public List<Integer> getIndexesOfRemovedFragments(int numFragments) {
			List<Integer> indexes = IntStream.range(0, numFragments).boxed()
					.collect(Collectors.toList());
			List<Integer> removed = new ArrayList<>();
			int numFragsToRemove = Math
					.min(numFragments, this.numFragsToRemove);
			for (int i = 0; i < numFragsToRemove; i++) {
				removed.add(indexes.remove(rand.nextInt(indexes.size())));
			}
			return removed;
		}

	}

	private RemovedFragmentSelector selector;

	public FragmentRemovalMutator(RemovedFragmentSelector selector) {
		this.selector = selector;
	}

	@Override
	public Mutation<FragmentationResult> generateMutation(
			FragmentationResult result, TlsContext context,
			ExecutionContext exContext) {
		int size = result.getFragments().size();
		Integer[] mapping = IntStream.range(0, size).boxed()
				.toArray(Integer[]::new);
		List<Integer> fragIndexesToRemove = selector
				.getIndexesOfRemovedFragments(size);
		fragIndexesToRemove.forEach(idx -> mapping[idx] = null);
		ReorderingMutation mutation = new ReorderingMutation(mapping);
		return mutation;
	}

}
