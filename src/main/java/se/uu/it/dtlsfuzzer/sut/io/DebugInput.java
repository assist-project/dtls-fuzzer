package se.uu.it.dtlsfuzzer.sut.io;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class DebugInput extends NamedTlsInput{

	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getTlsContext().setDtlsCurrentSendSequenceNumber(0);
		return null;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.UNKNOWN;
	}

}
