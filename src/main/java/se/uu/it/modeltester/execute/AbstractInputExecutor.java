package se.uu.it.modeltester.execute;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.GenericReceiveAction;
import se.uu.it.modeltester.Main;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public abstract class AbstractInputExecutor {
	private static final Logger LOGGER = LogManager.getLogger(Main.class
			.getName());

	public TlsOutput execute(TlsInput input, State state,
			ExecutionContext context) {
		ProtocolMessage message = input.generateMessage(state);
		LOGGER.info("Sending Message " + message.toCompactString());
		sendMessage(message, state, context);
		input.postSendUpdate(state);
		TlsOutput output = receiveOutput(state, context);
		input.postReceiveUpdate(output, state);
		return output;
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

	private TlsOutput extractOutput(State state, GenericReceiveAction action) {
		return new TlsOutput(action.getReceivedMessages());
	}
}
