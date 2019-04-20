package se.uu.it.modeltester.sut.io;

import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.execute.BasicInputExecutor;

public class ChangeCipherSpecInput extends TlsInput {
	
	public ChangeCipherSpecInput() {
		super(new BasicInputExecutor(), new ChangeCipherSpecMessage().toCompactString() );
	}

	public ProtocolMessage generateMessage(State state) {
		ChangeCipherSpecMessage ccs = new ChangeCipherSpecMessage(state.getConfig());
		return ccs;
	}

	@Override
	public void preUpdate(State state) {
		state.getTlsContext().getRecordLayer().updateEncryptionCipher();
	    state.getTlsContext().setWriteSequenceNumber(0);
	}

}
