package se.uu.it.modeltester.mutate.fragment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;

public class FragmentReplayMutation implements Mutation<FragmentationResult> {

	// private int numFragments;
	private int[] fragIndexes;

	public FragmentReplayMutation(int... fragIndexes) {
		this.fragIndexes = fragIndexes;
	}

	@Override
	public FragmentationResult mutate(FragmentationResult result,
			TlsContext context, ExecutionContext exContext) {
		List<DtlsHandshakeMessageFragment> fragmentsSent = exContext
				.getFragmentsSent();
		List<DtlsHandshakeMessageFragment> fragments = Stream.concat(
				fragmentsSent.stream(), result.getFragments().stream())
				.collect(Collectors.toList());
		List<DtlsHandshakeMessageFragment> replayedFragments = Arrays
				.stream(fragIndexes)
				.mapToObj(
						ind -> ind >= 0 ? fragments.get(ind) : fragments
								.get(fragments.size() + ind))
				.collect(Collectors.toList());

		List<DtlsHandshakeMessageFragment> newFragments = Stream.concat(
				result.getFragments().stream(), replayedFragments.stream())
				.collect(Collectors.toList());
		return new FragmentationResult(result.getMessage(), newFragments);
	}

	@Override
	public MutationType getType() {
		return MutationType.FRAGMENT_REPLAY;
	}

}
