package se.uu.it.modeltester.mutate;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.execute.MutatedInputExecutor;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class MutatedTLSInput extends TlsInput {
	
	private TlsInput input;

	public MutatedTLSInput(TlsInput input, List<Mutation<?>> mutations) {
		super(new MutatedInputExecutor(mutations));
		this.input = input;
	}
	
	@Override
	public ProtocolMessage generateMessage(State state) {
		return input.generateMessage(state);
	}
	
	public String toString() {
		return "MUTATED_" + input.toString() + "_" + ((MutatedInputExecutor) super.getExecutor()).getCompactMutationDescription();
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
