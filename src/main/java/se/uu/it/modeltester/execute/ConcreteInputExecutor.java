package se.uu.it.modeltester.execute;

import java.util.LinkedList;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

/**
 * A concrete input executor which implements the regular/non-altered way
 * of sending messages. 
 * 
 * It provides over-writable methods for every stage of the message sending process.
 */
public class ConcreteInputExecutor extends AbstractInputExecutor{

	private ExecuteInputHelper helper;
	
	public ConcreteInputExecutor() {
		helper = new ExecuteInputHelper();
	}
	
	@Override
	protected final void sendMessage(ProtocolMessage message, State state, ExecutionContext context) {
		message = prepareMessage(message, state, context);
		List<ProtocolMessage> messagesToSend = new LinkedList<>();

		if (message.isHandshakeMessage() && state.getTlsContext().getConfig().getDefaultSelectedProtocolVersion().isDTLS()) {
			FragmentationResult result = fragmentMessage(message, state, context);
			messagesToSend.addAll(result.getFragments());
		} else {
			messagesToSend.add(message);
		}
		
		PackingResult result = packMessages(messagesToSend, state, context);
		message.getHandler(state.getTlsContext()).adjustTlsContextAfterSerialize(message);
		helper.sendRecords(result.getRecords(), state);
	}
	
	protected ProtocolMessage prepareMessage(ProtocolMessage message, State state, ExecutionContext context) {
		helper.prepareMessage(message, state);
		return message;
	}
	
	protected FragmentationResult fragmentMessage(ProtocolMessage message, State state, ExecutionContext context) {
		FragmentationResult result = helper.fragmentMessage((HandshakeMessage) message, state);
		context.getStepContext().setFragmentationResult(result);
		return result;
	}
	
	protected PackingResult packMessages(List<ProtocolMessage> messagesToSend, State state, ExecutionContext context) {
		PackingResult result = helper.packMessages(messagesToSend, state);
		context.getStepContext().setPackingResult(result);
		return result;
	}
}
