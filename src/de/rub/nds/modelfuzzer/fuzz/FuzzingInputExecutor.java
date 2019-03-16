package de.rub.nds.modelfuzzer.fuzz;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public abstract class FuzzingInputExecutor extends InputExecutor {
	
	public FuzzingInputExecutor(FuzzingStrategy... strategies) {
		
	}
	
	protected abstract void sendMessage(ProtocolMessage message, State state);
}
