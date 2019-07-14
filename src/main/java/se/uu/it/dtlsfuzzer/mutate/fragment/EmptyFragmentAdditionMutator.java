package se.uu.it.dtlsfuzzer.mutate.fragment;
import java.util.Random;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.execute.FragmentationResult;
import se.uu.it.dtlsfuzzer.mutate.FragmentationMutator;
import se.uu.it.dtlsfuzzer.mutate.Mutation;

public class EmptyFragmentAdditionMutator extends FragmentationMutator {
	private Random rand;

	public EmptyFragmentAdditionMutator(long seed) {
		rand = new Random(seed);
	}

	@Override
	public Mutation<FragmentationResult> generateMutation(
			FragmentationResult result, TlsContext context,
			ExecutionContext exContext) {
		int fragIndex = rand.nextInt(result.getFragments().size());
		EmptyFragmentAdditionMutation mutation = new EmptyFragmentAdditionMutation(
				fragIndex);
		return mutation;
	}

	public String toString() {
		return "EmptyFragmentMutator";
	}

}
