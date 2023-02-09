package se.uu.it.dtlsfuzzer.sut.input;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.protocol.message.HelloVerifyRequestMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

public class HelloVerifyRequestInput extends DtlsInput {

	/**
	 * option for resetting the digests
	 */
	@XmlAttribute(name = "resetDigest", required = false)
	private boolean resetDigest = true;

	@XmlAttribute(name = "digestHR", required = false)
	private boolean digestHR = false;

	public HelloVerifyRequestInput() {
		super("HELLO_VERIFY_REQUEST");
	}

	@Override
	public TlsMessage generateMessage(State state, ExecutionContext context) {
		return new HelloVerifyRequestMessage(state.getConfig());
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

	public void postSendDtlsUpdate(State state, ExecutionContext context) {
		if (resetDigest) {
			state.getTlsContext().getDigest().reset();
		}
	}
}
