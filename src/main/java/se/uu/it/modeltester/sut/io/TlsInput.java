package se.uu.it.modeltester.sut.io;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.execute.AbstractInputExecutor;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput {
	
	@XmlTransient
	private AbstractInputExecutor inputExecutor;
	
	protected TlsInput(AbstractInputExecutor executor) {
		this.inputExecutor = executor;
	}
	

	/**
	 * Gets the executor for this input.
	 */
	public AbstractInputExecutor getExecutor() {
		return inputExecutor;
	}
	
	/**
	 * Generates a fresh message and updates the context.
	 */
	public abstract ProtocolMessage generateMessage(State state);
	
	/**
	 * Updates the context after sending the input.
	 */
	public void preUpdate(State state) {
	}
	
	/**
	 * Updates the context after receiving an output.
	 */
	public void postUpdate(TlsOutput output, State state) {
	}
	
	/**
	 * Generates an input string which is assumed to uniquely identify the input.
	 */
	public abstract String toString();
	
	/**
	 * The type of the input should correspond to the type of the message the input generates.
	 */
	public abstract TlsInputType getInputType();
}
