package se.uu.it.dtlsfuzzer.execute;

import java.io.IOException;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.UnknownMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.GenericReceiveAction;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

public abstract class AbstractInputExecutor {
	private static final Logger LOGGER = LogManager
			.getLogger(AbstractInputExecutor.class.getName());

	public TlsOutput execute(TlsInput input, State state,
			ExecutionContext context) {
		context.getStepContext().setInput(input);
		if (input.isEnabled(state, context)) {
			ProtocolMessage message = input.generateMessage(state);
			if (message != null) {
				LOGGER.info("Sending Message " + message.toCompactString());
				sendMessage(message, state, context);
				input.postSendUpdate(state, context);
			}
			TlsOutput output = receiveOutput(state, context);
			input.postReceiveUpdate(output, state, context);
			return output;
		} else {
			return TlsOutput.disabled();
		}
	}

	protected abstract void sendMessage(ProtocolMessage message, State state,
			ExecutionContext context);

	protected TlsOutput receiveOutput(State state, ExecutionContext context) {
		try {
			if (state.getTlsContext().getTransportHandler().isClosed()) {
				return TlsOutput.socketClosed();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return TlsOutput.socketClosed();
		}
		try {
			GenericReceiveAction action = new GenericReceiveAction("client");
			action.execute(state);
			context.getStepContext().setReceivedOutputMessages(
					action.getMessages());
			return extractOutput(state, action);
		} catch (Exception E) {
			E.printStackTrace();
			return TlsOutput.socketClosed();
		}
	}

	private boolean isResponseUnknown(GenericReceiveAction action) {
		if (action.getReceivedMessages().size() > 2) {
			return action.getReceivedMessages().stream()
					.allMatch(m -> m instanceof AlertMessage);
		}
		return false;
	}

	private TlsOutput extractOutput(State state, GenericReceiveAction action) {
		if (isResponseUnknown(action)) {
			return new TlsOutput(Arrays.asList(new UnknownMessage()));
		}
		if (action.getReceivedMessages().isEmpty()) {
			return TlsOutput.timeout();
		} else {

			// in case we find repeated occurrences of types of messages, we
			// coalesce them under +
			StringBuilder builder = new StringBuilder();
			String lastSeen = null;
			boolean skipStar = false;
			for (ProtocolMessage m : action.getReceivedMessages()) {
				if (lastSeen != null && lastSeen.equals(m.toCompactString())) {
					if (!skipStar) {
						// insert before ,
						builder.insert(builder.length() - 1, '+');
						skipStar = true;
					}
				} else {
					lastSeen = m.toCompactString();
					skipStar = false;
					builder.append(m.toCompactString());
					builder.append(",");
				}
			}

			String header = builder.substring(0, builder.length() - 1);

			return new TlsOutput(header);
		}
	}
}
