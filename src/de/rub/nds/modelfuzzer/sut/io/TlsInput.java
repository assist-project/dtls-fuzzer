package de.rub.nds.modelfuzzer.sut.io;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput {
	
	@XmlTransient
	private InputExecutor inputExecutor;
	
	TlsInput(InputExecutor executor) {
		this.inputExecutor = executor;
	}
	

	/**
	 * Gets the executor for this input.
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
