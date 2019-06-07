package se.uu.it.modeltester.mutate.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;

/**
 * Mutates the fragmentation result by re-ordering fragments according to the
 * given mapping.
 */
public class ReorderingMutation implements Mutation<FragmentationResult> {

	private Integer[] mapping;

	public ReorderingMutation(Integer[] mapping) {
		this.mapping = mapping;
	}

	/**
	 * Applies the stored mapping on the the list of dtls fragments in the
	 * fragmentation result. An example: fragments=[f0,f1,f2] mapping=[2,1,0]
	 * new_fragments
	 * =[fragments[mapping[0]],fragments[mapping[1]],fragments[mapping
	 * [2]]]=[f2,f1,f0]
	 * <p/>
	 * The mapping may contain null elements. Elements mapped to null are
	 * excluded. mapping=[2,null,0] new_fragments=[f2,f0]
	 */
	@Override
	public FragmentationResult mutate(FragmentationResult result,
			TlsContext context, ExecutionContext exContext) {
		if (result.getFragments().size() != mapping.length) {
			throw new FragmentationMutationException(String.format(
					"Number of fragments in mutation (%d) "
							+ "is not equal to the size of index mapping (%d)",
					result.getFragments().size(), mapping.length));
		}
		List<DtlsHandshakeMessageFragment> fragments = new ArrayList<DtlsHandshakeMessageFragment>(
				mapping.length);

		for (Integer i = 0; i < mapping.length; i++) {
			if (mapping[i] != null) {
				fragments.add(result.getFragments().get(mapping[i]));
			}
		}

		return new FragmentationResult(result.getMessage(), fragments);
	}

	@Override
	public MutationType getType() {
		return MutationType.FRAGMENT_REORDERING;
	}

	public String toString() {
		return "ReorderingMutation(" + Arrays.asList(mapping) + ")";
	}

}
