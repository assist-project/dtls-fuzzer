package se.uu.it.modeltester.mutate.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.mutate.FragmentationMutator;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutatorType;

public class FragmentOverlapMutator extends FragmentationMutator {

	private Random rand;

	public FragmentOverlapMutator(long seed) {
		rand = new Random(seed);
	}

	@Override
	public Mutation<FragmentationResult> generateMutation(
			FragmentationResult result, TlsContext context,
			ExecutionContext exContext) {
		Fragmentation fragmentation = Fragmentation
				.generateFromExistingMessageFragments(result.getFragments());
		Integer length = result.getMessage().getLength().getValue();
		List<Fragment> fragmentsWithOverlap = new ArrayList<>(fragmentation
				.getFragments().size());

		for (Fragment fragment : fragmentation.getFragments()) {
			int maxOverlap = length
					- (fragment.getOffset() + fragment.getLength());
			Integer overlap = maxOverlap > 0 ? rand.nextInt(maxOverlap) : 0;
			fragmentsWithOverlap.add(new Fragment(fragment.getOffset(),
					fragment.getLength() + overlap));
		}

		Fragmentation fragmentationWithOverlap = new Fragmentation(
				fragmentsWithOverlap);

		return new SplittingMutation(fragmentationWithOverlap);
	}

	@Override
	public MutatorType getType() {
		return MutatorType.FRAGMENTATION;
	}

}
