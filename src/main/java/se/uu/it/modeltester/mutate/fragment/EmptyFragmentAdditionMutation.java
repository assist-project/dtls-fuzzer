package se.uu.it.modeltester.mutate.fragment;

import java.util.LinkedList;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;

public class EmptyFragmentAdditionMutation
		implements
			Mutation<FragmentationResult> {

	private int index;

	public EmptyFragmentAdditionMutation(int index) {
		this.index = index;
	}

	@Override
	public FragmentationResult mutate(FragmentationResult result,
			TlsContext context, ExecutionContext exContext) {
		DtlsHandshakeMessageFragment currentFragment = result.getFragments()
				.get(index);
		HandshakeMessage message = result.getMessage();
		DtlsHandshakeMessageFragment emptyFragment = new DtlsHandshakeMessageFragment(
				message.getHandshakeMessageType(), new byte[]{});
		emptyFragment.getHandler(context).prepareMessage(emptyFragment);
		emptyFragment.setLength(message.getLength().getValue());
		emptyFragment.setFragmentLength(0);
		emptyFragment.setFragmentOffset(currentFragment.getFragmentOffset());
		emptyFragment.setCompleteResultingMessage(emptyFragment
				.getHandler(context).getSerializer(emptyFragment).serialize());
		LinkedList<DtlsHandshakeMessageFragment> newFragments = new LinkedList<>(
				result.getFragments());
		newFragments.add(index, emptyFragment);
		FragmentationResult newResult = new FragmentationResult(
				result.getMessage(), newFragments);
		return newResult;
	}

	@Override
	public MutationType getType() {
		return MutationType.EMPTY_FRAGMENT_ADDITION;
	}

	public String toString() {
		return "+" + new Fragment(0, 0).toString();
	}

}
