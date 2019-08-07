package se.uu.it.dtlsfuzzer.sut.io;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.modifiablevariable.bytearray.ByteArrayExplicitValueModification;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.dtls.MessageFragmenter;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;

public class ClientHelloInput extends NamedTlsInput {

	@XmlAttribute(name = "suite", required = true)
	private CipherSuite suite;

	/**
	 * option needed to learn DTLS implementations which use cookie-less
	 * handshake messages
	 */
	@XmlAttribute(name = "forceDigest", required = false)
	private boolean forceDigest = false;
	
	/**
	 * Include the latest session id in the client hello
	 */
	@XmlAttribute(name = "withSessionId", required = false)
	private boolean withSessionId = true;

	private ProtocolMessage message;

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
		if (suite.name().contains("EC")) {
			state.getConfig().setAddECPointFormatExtension(true);
			state.getConfig().setAddEllipticCurveExtension(true);
		} else {
			state.getConfig().setAddECPointFormatExtension(false);
			state.getConfig().setAddEllipticCurveExtension(false);
		}
		state.getTlsContext().getDigest().reset();
		
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		
		// we exclude the sessionId
		if (!withSessionId) {
			ModifiableByteArray sbyte = new ModifiableByteArray();
			sbyte.setModification(new ByteArrayExplicitValueModification(
					new byte[]{}));
			message.setSessionId(sbyte);
		}
		
		this.message = message;
		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

	public void postSendUpdate(State state, ExecutionContext context) {
		// the second conjunction is used in case TLS-Attacker is updated
		// to work also with 1-CH DTLS handshakes.
		// (in which case, the clienthello is digested)
		if (forceDigest
				&& state.getTlsContext().getDigest().getRawBytes().length == 0) {
			DtlsHandshakeMessageFragment fragment = new MessageFragmenter(state
					.getTlsContext().getConfig()).wrapInSingleFragment(
					(HandshakeMessage) message, state.getTlsContext());
			state.getTlsContext().getDigest()
					.append(fragment.getCompleteResultingMessage().getValue());
		}
	}

}
