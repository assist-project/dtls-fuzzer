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
	    Long writeSeqNumForCurrentEpoch = state.getTlsContext().getRecordLayer().getEncryptor().getRecordCipher(state.getTlsContext().getWriteEpoch()).getState().getWriteSequenceNumber();
		context.setWriteRecordNumberEpoch0(writeSeqNumForCurrentEpoch + 1);
	}

	public TlsMessage generateMessage(State state, ExecutionContext context) {
		ChangeCipherSpecMessage ccs = new ChangeCipherSpecMessage(
				state.getConfig());
		return ccs;
	}

	@Override
	public void postSendDtlsUpdate(State state, ExecutionContext context) {
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.CCS;
	}

}
