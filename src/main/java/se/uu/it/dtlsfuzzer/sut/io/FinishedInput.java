package se.uu.it.dtlsfuzzer.sut.io;

import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class FinishedInput extends NamedTlsInput {

	public FinishedInput() {
		super("FINISHED");
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		FinishedMessage message = new FinishedMessage();
		return message;
	}

	@Override
	public void postSendUpdate(State state) {
		state.getTlsContext().getDigest().reset();
		// we have to make this change for learning to scale
		state.getTlsContext().setDtlsNextSendSequenceNumber(
				state.getTlsContext().getDtlsCurrentSendSequenceNumber() + 1);
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}
}
