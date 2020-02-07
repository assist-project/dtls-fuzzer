package se.uu.it.dtlsfuzzer.sut.io;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
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

	@XmlAttribute(name = "suite", required = false)
	private CipherSuite suite;

	/**
	 * Resetting the cookie may prompt a long resumption, which is resumption
	 * including a server HelloVerifyRequest
	 */
	@XmlAttribute(name = "resetCookie", required = false)
	private boolean resetCookie = false;

	private void resetTransportHandler(State state) {
		ResetConnectionAction resetAction = new ResetConnectionAction();
		resetAction.setConnectionAlias(state.getTlsContext().getConnection()
				.getAlias());
		resetAction.execute(state);
		// we add the resets that should be in TLS-Attacker just to ensure
		// we don't relly on the specific TLS-Attacker version
		state.getTlsContext().setDtlsNextReceiveSequenceNumber(0);
		state.getTlsContext().setDtlsNextSendSequenceNumber(0);
		state.getTlsContext().setDtlsSendEpoch(0);
		state.getTlsContext().setDtlsNextReceiveEpoch(0);

		if (resetCookie) {
			state.getTlsContext().setDtlsCookie(null);
		}
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		// reset and resume the connection
		resetTransportHandler(state);
		if (suite != null) {
			state.getConfig().setDefaultClientSupportedCiphersuites(suite);
		}
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		message.setSessionId(state.getTlsContext().getChooser()
				.getServerSessionId());

		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
