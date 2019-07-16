package se.uu.it.dtlsfuzzer.sut.io;

import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class ClientHelloRenegotiation extends NamedTlsInput {

	public ClientHelloRenegotiation() {
		super("CLIENT_HELLO_RENEGOTIATION");
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getTlsContext().setDtlsNextSendSequenceNumber(0);
		state.getTlsContext().getDigest().reset();
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
