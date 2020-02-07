package se.uu.it.dtlsfuzzer.execute;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.GenericReceiveAction;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

public abstract class AbstractInputExecutor {
	private static final Logger LOGGER = LogManager
			.getLogger(AbstractInputExecutor.class.getName());

	/*
	 * Failure to decrypt shows up as a longer sequence of alert messages.
	 */
	private static final int MIN_ALERTS_IN_DECRYPTION_FAILURE = 3;

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
		if (action.getReceivedMessages().size() > MIN_ALERTS_IN_DECRYPTION_FAILURE) {
			return action.getReceivedMessages().stream()
					.allMatch(m -> m instanceof AlertMessage);
		}
		return false;
	}

	/*
	 * Failure to decrypt shows up as a longer sequence of alarm messages.
	 */
	private int unknownResponseLookahed(int currentIndex,
			List<ProtocolMessage> messages) {
		int nextIndex = currentIndex;
		if (messages.size() - currentIndex > MIN_ALERTS_IN_DECRYPTION_FAILURE) {
			ProtocolMessage message = messages.get(nextIndex);
			while (message instanceof AlertMessage) {
				nextIndex++;
				message = messages.get(nextIndex);
			}
			if (nextIndex - currentIndex > MIN_ALERTS_IN_DECRYPTION_FAILURE)
				return nextIndex;
		}
		return -1;
	}

	private TlsOutput extractOutput(State state, GenericReceiveAction action) {
		if (isResponseUnknown(action)) {
			return TlsOutput.unknown();
		}
		if (action.getReceivedMessages().isEmpty()) {
			return TlsOutput.timeout();
		} else {
			// in case we find repeated occurrences of types of messages, we
			// coalesce them under +
			StringBuilder builder = new StringBuilder();
			String lastSeen = null;
			boolean skipStar = false;
			int i = 0;

			for (i = 0; i < action.getReceivedMessages().size(); i++) {
				// checking for cases of decryption failures, which which case
				// we add an unknown message
				int nextIndex = unknownResponseLookahed(i,
						action.getReceivedMessages());
				if (nextIndex > 0) {
					builder.append(TlsOutput.unknown().getMessageHeader());
					builder.append(",");
					i = nextIndex;
				}

				// Then we check for repeating outputs (some implementations can
				// generate a non-deterministic number of messages)"
				ProtocolMessage m = action.getReceivedMessages().get(i);
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
