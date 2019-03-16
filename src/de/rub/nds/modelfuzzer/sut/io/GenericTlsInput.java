package de.rub.nds.modelfuzzer.sut.io;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class GenericTlsInput extends TlsInput{
	
	private ProtocolMessage message;

	public GenericTlsInput(ProtocolMessage protocolMessage) {
		super(new InputExecutor());
		message = protocolMessage;
	}

	public ProtocolMessage generateMessage(State state) {
		return message;
	}
	
	public String toString() {
		return message.toCompactString(); 
	}

	public void postUpdate(TlsOutput output, State state) {
	}

}
