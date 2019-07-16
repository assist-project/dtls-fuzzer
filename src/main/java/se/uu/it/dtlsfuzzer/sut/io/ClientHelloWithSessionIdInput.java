package se.uu.it.dtlsfuzzer.sut.io;

import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.ResetConnectionAction;

public class ClientHelloWithSessionIdInput extends NamedTlsInput {

	public ClientHelloWithSessionIdInput() {
		super("CLIENT_HELLO_SR");
	}

	protected ClientHelloWithSessionIdInput(String name) {
		super(name);
	}

	private void resetTransportHandler(State state) {
		ResetConnectionAction resetAction = new ResetConnectionAction();
		resetAction.setConnectionAlias(state.getTlsContext().getConnection()
				.getAlias());
		resetAction.execute(state);
		state.getTlsContext().setDtlsCookie(new byte[]{});
		state.getTlsContext().setDtlsNextReceiveSequenceNumber(0);
		state.getTlsContext().setDtlsNextSendSequenceNumber(0);
		state.getTlsContext().setDtlsSendEpoch(0);
		state.getTlsContext().setDtlsNextReceiveEpoch(0);
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		if (!state.getTlsContext().getSessionList().isEmpty()) {
			// reset and resume the connection
			resetTransportHandler(state);
			message.setCookie(new byte[]{});
			message.setSessionId(state.getTlsContext().getSessionList()
					.get(state.getTlsContext().getSessionList().size() - 1)
					.getSessionId());
		}

		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
