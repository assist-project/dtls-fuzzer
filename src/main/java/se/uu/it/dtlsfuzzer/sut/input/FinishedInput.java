package se.uu.it.dtlsfuzzer.sut.input;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;
import se.uu.it.dtlsfuzzer.sut.output.ModelOutputs;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class FinishedInput extends DtlsInput {

	@XmlAttribute(name = "resetMSeq", required = true)
	private boolean resetMSeq = false;

	public FinishedInput() {
		super("FINISHED");
	}

	@Override
	public ProtocolMessage generateMessage(State state, ExecutionContext context) {
		// Uncomment line to print digest, TODO remove this when polishing things up
		//System.out.println(ArrayConverter.bytesToHexString(state.getTlsContext().getDigest().getRawBytes()));
		FinishedMessage message = new FinishedMessage();
		return message;
	}

	@Override
	public void postSendDtlsUpdate(State state, ExecutionContext context) {
		state.getTlsContext().getDigest().reset();
		// we have to make this change for learning to scale
		state.getTlsContext().setDtlsNextSendSequenceNumber(
				state.getTlsContext().getDtlsCurrentSendSequenceNumber() + 1);
	}

	public TlsOutput postReceiveUpdate(TlsOutput output, State state,
			ExecutionContext context) {
		if (resetMSeq) {
			if (ModelOutputs.hasChangeCipherSpec(output)) {
				state.getTlsContext().setDtlsNextSendSequenceNumber(0);
			}
		}
		return super.postReceiveUpdate(output, state, context);
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}
}
