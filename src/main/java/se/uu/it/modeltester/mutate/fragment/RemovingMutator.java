package se.uu.it.modeltester.mutate.fragment;

import java.util.stream.IntStream;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.mutate.FragmentationMutator;
import se.uu.it.modeltester.mutate.Mutation;

public class RemovingMutator extends FragmentationMutator {

	@Override
	public Mutation<FragmentationResult> generateMutation(FragmentationResult result, TlsContext context) {
		int size = result.getFragments().size();
		int [] mapping = IntStream.range(0, size).toArray();
		
		return null;
	}

}
