package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class DtlsInputWrapper extends DtlsInput{
	private DtlsInput input;


	public DtlsInputWrapper(DtlsInput input) {
		this.input = input;
	}
	@Override
	public TlsInputType getInputType() {
		return input.getInputType();
	}

	public DtlsInput getInput() {
		return input;
	}


	public void setEpoch(Integer epoch) {
		input.setEpoch(epoch);
	}

	public Integer getEpoch() {
		return input.getEpoch();
	}

	public void setEncryptionEnabled(boolean encrypted) {
		input.setEncryptionEnabled(encrypted);
	}

	public boolean isEncryptionEnabled() {
		return input.isEncryptionEnabled();
	}

	@Override
	public TlsMessage generateMessage(State state, ExecutionContext context) {
		return input.generateMessage(state, context);
	}

	public void preSendDtlsUpdate(State state, ExecutionContext context) {
		input.preSendDtlsUpdate(state, context);
	}

	public void postSendDtlsUpdate(State state, ExecutionContext context) {
		input.postSendDtlsUpdate(state, context);
	}

	public TlsOutput postReceiveUpdate(TlsOutput output, State state,
			ExecutionContext context) {
		return input.postReceiveUpdate(output, state, context);
	}
}
