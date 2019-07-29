package se.uu.it.dtlsfuzzer.mutate;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.execute.MutatingInputExecutor;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsInputType;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

public class MutatingTlsInput extends TlsInput {

	private TlsInput input;

	public MutatingTlsInput(TlsInput input, List<Mutator<?>> mutators) {
		super();
		this.setPreferredExecutor(new MutatingInputExecutor(mutators));
		this.input = input;
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		return input.generateMessage(state);
	}

	public String toString() {
		return "MUTATING_" + input.toString() + "_"
				+ getExecutorAsMutating().getCompactMutatorDescription();
	}

	public void postSendUpdate(State state, ExecutionContext context) {
		input.postSendUpdate(state, context);
	}

	public void postReceiveUpdate(TlsOutput output, State state, ExecutionContext context) {
		input.postReceiveUpdate(output, state, context);
	}

	public MutatingInputExecutor getExecutorAsMutating() {
		return ((MutatingInputExecutor) super.getPreferredExecutor());
	}

	public TlsInput getInput() {
		return input;
	}

	@Override
	public TlsInputType getInputType() {
		return input.getInputType();
	}
}
