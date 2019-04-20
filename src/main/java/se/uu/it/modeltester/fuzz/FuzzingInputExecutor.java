package se.uu.it.modeltester.fuzz;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.sut.InputExecutor;

public abstract class FuzzingInputExecutor extends InputExecutor {
	
	public FuzzingInputExecutor(FuzzingStrategy... strategies) {
		
	}
	
	protected abstract void sendMessage(ProtocolMessage message, State state);
}
