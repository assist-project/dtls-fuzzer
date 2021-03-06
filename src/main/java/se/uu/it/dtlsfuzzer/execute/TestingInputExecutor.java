package se.uu.it.dtlsfuzzer.execute;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

/**
 * A concrete input executor which implements the regular/non-altered way of
 * sending messages.
 * 
 * It provides over-writable methods for every stage of the message sending
 * process, and updates the execution context.
 * 
 * This can form a good basis for conformance testing of learned models, since
 * one can vary how inputs are executed or mutate the messages generated.
 */
public class TestingInputExecutor extends AbstractInputExecutor {

	private ExecuteInputHelper helper;
	private boolean sequentialSend;

	public TestingInputExecutor() {
		helper = new ExecuteInputHelper();
		sequentialSend = false;
	}

	@Override
	protected final void sendMessage(ProtocolMessage message, State state,
			ExecutionContext context) {
		message = prepareMessage(message, state, context);
		List<ProtocolMessage> messagesToSend = new LinkedList<>();

		if (message.isHandshakeMessage()
				&& state.getTlsContext().getConfig()
						.getDefaultSelectedProtocolVersion().isDTLS()) {
			FragmentationResult result = fragmentMessage(message, state,
					context);
			context.getStepContext().setFragmentationResult(result);
			messagesToSend.addAll(result.getFragments());
		} else {
			messagesToSend.add(message);
		}
		PackingResult result = packMessages(messagesToSend, state, context);
		context.getStepContext().setPackingResult(result);
		message.getHandler(state.getTlsContext())
				.adjustTlsContextAfterSerialize(message);

		if (sequentialSend)
			result.getRecords().forEach(
					r -> helper.sendRecords(Arrays.asList(r), state));
		else
			helper.sendRecords(result.getRecords(), state);

	}

	protected ProtocolMessage prepareMessage(ProtocolMessage message,
			State state, ExecutionContext context) {
		helper.prepareMessage(message, state);
		return message;
	}

	protected FragmentationResult fragmentMessage(ProtocolMessage message,
			State state, ExecutionContext context) {
		FragmentationResult result = helper.fragmentMessage(
				(HandshakeMessage) message, state);
		return result;
	}

	protected PackingResult packMessages(List<ProtocolMessage> messagesToSend,
			State state, ExecutionContext context) {
		PackingResult result = helper.packMessages(messagesToSend, state);
		return result;
	}
}
