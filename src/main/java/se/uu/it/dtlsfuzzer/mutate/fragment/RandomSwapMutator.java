package se.uu.it.dtlsfuzzer.mutate.fragment;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.execute.FragmentationResult;
import se.uu.it.dtlsfuzzer.mutate.FragmentationMutator;
import se.uu.it.dtlsfuzzer.mutate.Mutation;

public class RandomSwapMutator extends FragmentationMutator {

	private Random rand;

	public RandomSwapMutator(long seed) {
		rand = new Random(seed);
	}

	@Override
	public Mutation<FragmentationResult> generateMutation(
			FragmentationResult result, TlsContext context,
			ExecutionContext exContext) {
		List<Integer> mapping = IntStream
				.range(0, result.getFragments().size()).boxed()
				.collect(Collectors.toList());
		Collections.shuffle(mapping, rand);
		return new ReorderingMutation(mapping.toArray(new Integer[mapping
				.size()]));
	}

	public String toString() {
		return "RandomSwapMutator";
	}
}
