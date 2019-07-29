package se.uu.it.dtlsfuzzer.mutate;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.execute.MutatedInputExecutor;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsInputType;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

public class MutatedTlsInput extends TlsInput {

	private TlsInput input;

	public MutatedTlsInput(TlsInput input, List<Mutation<?>> mutations) {
		super();
		this.setPreferredExecutor(new MutatedInputExecutor(mutations));
		this.input = input;
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		return input.generateMessage(state);
	}

	public String toString() {
		return "MUTATED_"
				+ input.toString()
				+ "_"
				+ ((MutatedInputExecutor) super.getPreferredExecutor())
						.getCompactMutationDescription();
	}

	public void postSendUpdate(State state, ExecutionContext context) {
		input.postSendUpdate(state, context);
	}

	public void postReceiveUpdate(TlsOutput output, State state, ExecutionContext context) {
		input.postReceiveUpdate(output, state, context);
	}

	public List<Mutation<?>> getMutations() {
		return ((MutatedInputExecutor) super.getPreferredExecutor())
				.getMutations();
	}

	public TlsInput getInput() {
		return input;
	}

	@Override
	public TlsInputType getInputType() {
		return input.getInputType();
	}

}
