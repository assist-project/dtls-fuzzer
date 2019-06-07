package se.uu.it.modeltester.sut.io;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class ClientHelloInput extends NamedTlsInput {

	@XmlAttribute(name = "suite", required = true)
	private CipherSuite suite;

	public ClientHelloInput() {
		super("CLIENT_HELLO");
	}

	public ClientHelloInput(CipherSuite cipherSuite) {
		super(cipherSuite.name() + "_CLIENT_HELLO");
		this.suite = cipherSuite;
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getConfig().setDefaultSelectedCipherSuite(suite);
		state.getConfig().setDefaultServerSupportedCiphersuites(suite);
		state.getConfig().setDefaultClientSupportedCiphersuites(
				Arrays.asList(suite));
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		// message.getHandler(state.getTlsContext()).prepareMessage(message);

		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
