package de.rub.nds.modelfuzzer.sut.io;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class FuzzedTlsInput extends TlsInput{

	private TlsInput input;

	public FuzzedTlsInput(TlsInput input, InputExecutor executor) {
		super(executor);
		this.input = input;
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		return input.generateMessage(state);
	}
	
	public String toString() {
		return "FUZZED_"+input.toString();
	}

	public void postUpdate(TlsOutput output, State state) {
		input.postUpdate(output, state);
	}
}
