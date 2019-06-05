package se.uu.it.modeltester.mutate.record;

import java.util.ArrayList;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecuteInputHelper;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.PackingResult;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;

public class MessageDupMutation  implements Mutation<PackingResult>{
	
	private int index;

	public MessageDupMutation(int index) {
		this.index = index;
	}

	@Override
	public PackingResult mutate(PackingResult result, TlsContext context, ExecutionContext exContext) {
		List<ProtocolMessage> mSent = new ArrayList<>(exContext.getMessagesSent());
		mSent.addAll(result.getMessages());
		ProtocolMessage msgToDup = mSent.get(index >= 0 ? index : (mSent.size() + index));
		List<ProtocolMessage> mToSend = new ArrayList<>(result.getMessages());
		mToSend.add(msgToDup);
		PackingResult pResult = new ExecuteInputHelper().packMessages(mToSend, context);
		return pResult;
	}

	@Override
	public MutationType getType() {
		// TODO Auto-generated method stub
		return MutationType.MESSAGE_SWAP;
	}

}
