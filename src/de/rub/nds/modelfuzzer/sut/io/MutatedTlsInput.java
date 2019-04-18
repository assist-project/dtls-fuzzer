package de.rub.nds.modelfuzzer.sut.io;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class MutatedTlsInput extends TlsInput{

	private TlsInput input;

	public MutatedTlsInput(TlsInput input, InputExecutor executor) {
		super(executor);
		this.input = input;
	}
	
	@Override
	public ProtocolMessage generateMessage(State state) {
		return input.generateMessage(state);
	}
	
	public String toString() {
		return "MUTATED_"+input.toString();
	}
	
	public void preUpdate(State state) {
		input.preUpdate(state);
	}

	public void postUpdate(TlsOutput output, State state) {
		input.postUpdate(output, state);
	}
	
	public TlsInput getInput() {
		return input;
	}
}
