package se.uu.it.modeltester.sut.io;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.execute.BasicInputExecutor;
import se.uu.it.modeltester.execute.InputExecutor;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput {
	
	@XmlTransient
	private InputExecutor inputExecutor;
	
	/**
	 * The name with which the input can be referred 
	 */
	@XmlAttribute(name = "name", required = true)
	private String name;
	
	TlsInput(InputExecutor executor, String name) {
		this.inputExecutor = executor;
		this.name = name;
	}
	

	/**
	 * Gets the executor for this input.
	 */
	public InputExecutor getExecutor() {
		return inputExecutor;
	}
	
	public void setExecutor(InputExecutor executor) {
		this.inputExecutor = executor;
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
	 * Generates an input string which should be unique for the input.
	 */
	public String toString() {
		return name;
	}
	
}
