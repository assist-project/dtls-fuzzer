package se.uu.it.dtlsfuzzer.sut.io;

import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;

public class ChangeCipherSpecInput extends NamedTlsInput {

	public ChangeCipherSpecInput() {
		super("CHANGE_CIPHER_SPEC");
	}

	public ProtocolMessage generateMessage(State state) {
		ChangeCipherSpecMessage ccs = new ChangeCipherSpecMessage(
				state.getConfig());
		return ccs;
	}

	@Override
	public void postSendUpdate(State state, ExecutionContext context) {
		state.getTlsContext().getRecordLayer().updateEncryptionCipher();
		state.getTlsContext().setWriteSequenceNumber(0);
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.CCS;
	}

}
