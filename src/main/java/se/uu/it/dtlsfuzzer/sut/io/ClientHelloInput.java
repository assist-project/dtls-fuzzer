package se.uu.it.dtlsfuzzer.sut.io;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class ClientHelloInput extends NamedTlsInput {

	@XmlAttribute(name = "suite", required = true)
	private CipherSuite suite;

	@XmlAttribute(name = "resume", required = false)
	private boolean resume = false;

	public ClientHelloInput() {
		super("CLIENT_HELLO");
	}

	public ClientHelloInput(CipherSuite cipherSuite) {
		super(cipherSuite.name() + "_CLIENT_HELLO");
		this.suite = cipherSuite;
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getConfig().setDefaultClientSupportedCiphersuites(
				Arrays.asList(suite));
		state.getTlsContext().getDigest().reset();
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		if (resume && !state.getTlsContext().getSessionList().isEmpty()) {
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
