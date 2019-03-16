package de.rub.nds.modelfuzzer.sut.io;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class ChangeCipherSpecInput extends TlsInput {
	
	public ChangeCipherSpecInput() {
		super(new InputExecutor() );
	}

	public ProtocolMessage generateMessage(State state) {
		ChangeCipherSpecMessage ccs = new ChangeCipherSpecMessage(state.getConfig());
		return ccs;
	}

	@Override
	public String toString() {
		return new ChangeCipherSpecMessage().toCompactString();
	}

	@Override
	public void postUpdate(TlsOutput output, State state) {
		state.getTlsContext().getRecordLayer().updateEncryptionCipher();
	    state.getTlsContext().setWriteSequenceNumber(0);
	}

}
