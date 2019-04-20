package se.uu.it.modeltester.test;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.execute.BasicInputExecutor;

public abstract class FuzzingInputExecutor extends BasicInputExecutor {
	
	public FuzzingInputExecutor(FuzzingStrategy... strategies) {
		
	}
	
	protected abstract void sendMessage(ProtocolMessage message, State state);
}
