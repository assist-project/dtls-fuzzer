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
 * TODO this could be turned into a hookable input executor, since with mutating
 * executors extending it.
 */
public class ConcreteInputExecutor extends AbstractInputExecutor{

	public ConcreteInputExecutor() {
	}
	
	@Override
	protected void sendMessage(ProtocolMessage message, State state) {
		ExecuteInputHelper helper = new ExecuteInputHelper();
		helper.prepareMessage(message, state);
		List<ProtocolMessage> messagesToSend = new LinkedList<>();

		if (message.isHandshakeMessage() && state.getTlsContext().getConfig().getDefaultSelectedProtocolVersion().isDTLS()) {
			FragmentationResult result = helper.fragmentMessage((HandshakeMessage) message, state);
			messagesToSend.addAll(result.getFragments());
		} else {
			messagesToSend.add(message);
		}
		
		PackingResult result = helper.packMessages(messagesToSend, state);
		message.getHandler(state.getTlsContext()).adjustTlsContextAfterSerialize(message);
		helper.sendRecords(result.getRecords(), state);
	}
	
}
