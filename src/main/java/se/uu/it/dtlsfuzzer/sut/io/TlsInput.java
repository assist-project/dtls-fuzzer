package se.uu.it.dtlsfuzzer.sut.io;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.execute.AbstractInputExecutor;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput {

	@XmlTransient
	private AbstractInputExecutor preferredExecutor = null;

	protected TlsInput() {
	}

	/**
	 * Returns the preferred executor for this input, or null, if there isn't
	 * one.
	 */
	public AbstractInputExecutor getPreferredExecutor() {
		return preferredExecutor;
	}

	public void setPreferredExecutor(AbstractInputExecutor preferredExecutor) {
		this.preferredExecutor = preferredExecutor;
	}

	/**
	 * Enables the input for execution.
	 */
	public boolean isEnabled(State state, ExecutionContext context) {
		return true;
	}

	/**
	 * Generates a fresh prepared message and updates the context.
	 */
	public abstract ProtocolMessage generateMessage(State state);

	/**
	 * Updates the context after sending the input.
	 */
	public void postSendUpdate(State state) {
	}

	/**
	 * Updates the context after receiving an output.
	 */
	// TODO it would be better to have abstracting/concretizing mapper
	// components.
	public void postReceiveUpdate(TlsOutput output, State state) {
	}

	/**
	 * Generates an input string which is assumed to uniquely identify the
	 * input.
	 */
	public abstract String toString();

	/**
	 * The type of the input should correspond to the type of the message the
	 * input generates.
	 */
	public abstract TlsInputType getInputType();
}
