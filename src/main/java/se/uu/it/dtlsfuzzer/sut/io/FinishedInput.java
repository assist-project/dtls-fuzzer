package se.uu.it.dtlsfuzzer.sut.io;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;

public class FinishedInput extends NamedTlsInput {
	
	@XmlAttribute(name = "resetMSeq", required = true)
	private boolean resetMSeq = false;

	public FinishedInput() {
		super("FINISHED");
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		FinishedMessage message = new FinishedMessage();
		return message;
	}

	@Override
	public void postSendUpdate(State state, ExecutionContext context) {
		state.getTlsContext().getDigest().reset();
		// we have to make this change for learning to scale
		state.getTlsContext().setDtlsNextSendSequenceNumber(
				state.getTlsContext().getDtlsCurrentSendSequenceNumber() + 1);
	}
	
	public void postReceiveUpdate(TlsOutput output, State state,
			ExecutionContext context) {
		if (resetMSeq) {
			if (output.getMessageHeader().contains("CHANGE_CIPHER_SPEC")) {
				state.getTlsContext().setDtlsNextSendSequenceNumber(0);
			} 
		}
	}
	
	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}
}
