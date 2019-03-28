package de.rub.nds.modelfuzzer.sut.io;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public abstract class TlsInput {
	
	private InputExecutor inputExecutor;
	
	TlsInput(InputExecutor executor) {
		this.inputExecutor = executor;
	}
	

	/**
	 * Generates an executor for this inputs.
	 */
	public InputExecutor getExecutor() {
		return inputExecutor;
	}
	
	/**
	 * Generates a fresh message and updates the context.
	 */
	public abstract ProtocolMessage generateMessage(State state);
	
	/**
	 * Updates the context after receiving an output.
	 */
	public abstract void postUpdate(TlsOutput output, State state);
	
	/**
	 * Prints an input string corresponding to the message. Input strings should be unique for each input.
	 */
	public abstract String toString();
	
}
