package se.uu.it.dtlsfuzzer.sut.io;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.modifiablevariable.bytearray.ByteArrayExplicitValueModification;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class ClientHelloRenegotiationInput extends NamedTlsInput {
	
	/**
	 * The name with which the input can be referred
	 */
	@XmlAttribute(name = "short", required = false)
	private boolean isShort =  false;
	
	@XmlAttribute(name = "suite", required = false)
	private CipherSuite suite = null;
	

	public ClientHelloRenegotiationInput() {
		super("CLIENT_HELLO_RENEGOTIATION");
	}
	
	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getTlsContext().setDtlsNextSendSequenceNumber(0);
		state.getTlsContext().getDigest().reset();
		state.getTlsContext().setDtlsNextReceiveSequenceNumber(0);
		if (suite != null) {
			state.getConfig().setDefaultClientSupportedCiphersuites(suite);
		}
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		if (!isShort) {
			ModifiableByteArray sbyte = new ModifiableByteArray();
			sbyte.setModification(new ByteArrayExplicitValueModification(new byte [] {}));
			message.setSessionId(sbyte);
		}
		
		// mbedtls will only engage in renegotiation if the cookie is empty
		ModifiableByteArray cbyte = new ModifiableByteArray();
		cbyte.setModification(new ByteArrayExplicitValueModification(new byte [] {}));
		message.setCookie(cbyte);
		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
