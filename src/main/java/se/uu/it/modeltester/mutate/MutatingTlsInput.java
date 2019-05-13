package se.uu.it.modeltester.mutate;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.execute.MutatingInputExecutor;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class MutatingTlsInput extends TlsInput{

	private TlsInput input;

	public MutatingTlsInput(TlsInput input) {
		super(new MutatingInputExecutor(), "MUTATED_"+input.toString());
		this.input = input;
	}
	
	@Override
	public ProtocolMessage generateMessage(State state) {
		return input.generateMessage(state);
	}
	
	public String toString() {
		return "MUTATING_"+((MutatingInputExecutor) super.getExecutor()).getCompactMutatorDescription() + "_"+input.toString();
	}
	
	public void preUpdate(State state) {
		input.preUpdate(state);
	}

	public void postUpdate(TlsOutput output, State state) {
		input.postUpdate(output, state);
	}
	
	public boolean addMutator(Mutator<?> mutator) {
		return ((MutatingInputExecutor) super.getExecutor()).addMutator(mutator);
	}
	
	public TlsInput getInput() {
		return input;
	}
}
