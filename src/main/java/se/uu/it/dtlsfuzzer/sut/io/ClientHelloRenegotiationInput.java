package se.uu.it.dtlsfuzzer.sut.io;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.modifiablevariable.bytearray.ByteArrayExplicitValueModification;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;

public class ClientHelloRenegotiationInput extends NamedTlsInput {

	static enum Enabled {
		OWN_EPOCH_CHANGE, SERVER_EPOCH_CHANGE, ALWAYS, ONCE
	}

	@XmlAttribute(name = "short", required = false)
	private boolean isShort = false;

	@XmlAttribute(name = "suite", required = false)
	private CipherSuite suite = null;

	@XmlAttribute(name = "enabled", required = false)
	private Enabled enabled = Enabled.ALWAYS;

	@XmlAttribute(name = "withCookie", required = false)
	private boolean withCookie = true;

	@XmlAttribute(name = "resetMSeq", required = false)
	private boolean resetMSeq = true;

	public ClientHelloRenegotiationInput() {
		super("CLIENT_HELLO_RENEGOTIATION");
	}

	public boolean isEnabled(State state, ExecutionContext context) {
		switch (enabled) {
			case OWN_EPOCH_CHANGE :
				// send epoch is 1 or more
				return state.getTlsContext().getDtlsSendEpoch() > 0;
			case SERVER_EPOCH_CHANGE :
				// receive epoch is 1 or more
				return state.getTlsContext().getDtlsNextReceiveEpoch() > 0;
			case ONCE :
				return context.getStepContexes()
						.subList(0, context.getStepCount() - 1).stream()
						.noneMatch(s -> s.getInput().equals(this));
			default :
				return true;
		}
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getTlsContext().getDigest().reset();
		if (resetMSeq)
			state.getTlsContext().setDtlsNextSendSequenceNumber(0);
		state.getTlsContext().setDtlsNextReceiveSequenceNumber(0);
		if (suite != null) {
			state.getConfig().setDefaultClientSupportedCiphersuites(suite);
		}
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		if (!isShort) {
			ModifiableByteArray sbyte = new ModifiableByteArray();
			sbyte.setModification(new ByteArrayExplicitValueModification(
					new byte[]{}));
			message.setSessionId(sbyte);
		}

		// mbedtls will only engage in renegotiation if the cookie is empty
		if (!withCookie && state.getTlsContext().getDtlsCookie() != null) {
			ModifiableByteArray sbyte = new ModifiableByteArray();
			sbyte.setModification(new ByteArrayExplicitValueModification(
					new byte[]{}));
			message.setCookie(sbyte);
		}

		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
