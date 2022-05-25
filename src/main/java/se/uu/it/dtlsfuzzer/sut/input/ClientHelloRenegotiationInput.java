package se.uu.it.dtlsfuzzer.sut.input;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.modifiablevariable.bytearray.ByteArrayExplicitValueModification;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;
import se.uu.it.dtlsfuzzer.sut.output.ModelOutputs;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class ClientHelloRenegotiationInput extends TlsInput {

	static enum Enabled {
		OWN_EPOCH_CHANGE, SERVER_EPOCH_CHANGE, ALWAYS, ONCE, ON_SERVER_HELLO
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
				return state.getTlsContext().getDtlsWriteEpoch() > 0;
			case SERVER_EPOCH_CHANGE :
				// receive epoch is 1 or more
				return state.getTlsContext().getDtlsReceiveEpoch() > 0;
			case ONCE :
				return context.getStepContexes()
						.subList(0, context.getStepCount() - 1).stream()
						.noneMatch(s -> s.getInput().equals(this));
			default :
				return true;
		}
	}

	@Override
	public TlsMessage generateMessage(State state, ExecutionContext context) {
		state.getTlsContext().getDigest().reset();
		if (resetMSeq) {
			state.getTlsContext().setDtlsWriteHandshakeMessageSequence(0);
		}
		state.getTlsContext().setReadSequenceNumber(0);
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

	public TlsOutput postReceiveUpdate(TlsOutput output, State state,
			ExecutionContext context) {
		switch (enabled) {
			case ON_SERVER_HELLO :
				if (!ModelOutputs.hasServerHello(output)) {
					context.getStepContext().disable();
				}
				break;
			default :
				break;
		}
		return super.postReceiveUpdate(output, state, context);
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
