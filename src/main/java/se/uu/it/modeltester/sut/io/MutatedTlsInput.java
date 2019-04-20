package se.uu.it.modeltester.sut.io;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.execute.BasicInputExecutor;

public class MutatedTlsInput extends TlsInput{

	private TlsInput input;

	public MutatedTlsInput(TlsInput input, BasicInputExecutor executor) {
		super(executor, "MUTATED_"+input.toString());
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
