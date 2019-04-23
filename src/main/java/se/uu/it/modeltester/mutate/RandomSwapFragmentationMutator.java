package se.uu.it.modeltester.mutate;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.FragmentationResult;

public class RandomSwapFragmentationMutator extends FragmentationMutator{
	
	private Random rand;

	public RandomSwapFragmentationMutator(long seed) {
		rand = new Random(seed);
	}

	@Override
	public Mutation<FragmentationResult> generateMutation(FragmentationResult result, TlsContext context) {
		List<Integer> mapping = IntStream.range(0, result.getFragments().size()).boxed().collect(Collectors.toList());
		Collections.shuffle(mapping, rand);
		return new MappingFragmentationMutation(mapping.toArray(new Integer[mapping.size()]));
	}
	
}
