package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

public class ChangeCipherSpecInput extends DtlsInput {

	public ChangeCipherSpecInput() {
		super("CHANGE_CIPHER_SPEC");
	}
	
	@Override
	public void preSendDtlsUpdate(State state, ExecutionContext context) {
		context.setWriteRecordNumberEpoch0(state.getTlsContext().getWriteSequenceNumber() + 1);
	}

	public TlsMessage generateMessage(State state, ExecutionContext context) {
		ChangeCipherSpecMessage ccs = new ChangeCipherSpecMessage(
				state.getConfig());
		return ccs;
	}

	@Override
	public void postSendDtlsUpdate(State state, ExecutionContext context) {
		state.getTlsContext().getRecordLayer().updateEncryptionCipher();
		state.getTlsContext().setWriteSequenceNumber(0);
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.CCS;
	}

}
