package de.rub.nds.modelfuzzer.sut.io;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class FinishedInput extends TlsInput{
	
	public FinishedInput() {
		super(new InputExecutor());
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		FinishedMessage message = new FinishedMessage();
		return message;
	}

	@Override
	public String toString() {
		return "FINISHED";
	}

	@Override
	public void postUpdate(TlsOutput output, State state) {
		state.getTlsContext().getDigest().reset();
		// we have to make this change for learning to scale
		state.getTlsContext().setDtlsNextSendSequenceNumber(state.getTlsContext().getDtlsCurrentSendSequenceNumber() + 1);
	}
}
