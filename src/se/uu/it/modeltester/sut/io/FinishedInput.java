package se.uu.it.modeltester.sut.io;

import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.sut.InputExecutor;

public class FinishedInput extends TlsInput{
	
	public FinishedInput() {
		super(new InputExecutor(), "FINISHED");
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		FinishedMessage message = new FinishedMessage();
		return message;
	}

	@Override
	public void preUpdate(State state) {
		state.getTlsContext().getDigest().reset();
		// we have to make this change for learning to scale
		state.getTlsContext().setDtlsNextSendSequenceNumber(state.getTlsContext().getDtlsCurrentSendSequenceNumber() + 1);
	}
}
