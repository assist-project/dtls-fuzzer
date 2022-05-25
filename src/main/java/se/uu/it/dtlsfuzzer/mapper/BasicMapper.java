package se.uu.it.dtlsfuzzer.mapper;

import java.io.IOException;

import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.SendAction;
import se.uu.it.dtlsfuzzer.config.MapperConfig;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

/**
 * A lightweight mapper which sends messages by calling the
 * sendMessages functionality on the SendMessageHelper.
 * 
 * It does not update the execution context. 
 */
public class BasicMapper extends AbstractMapper {
	
	public BasicMapper(MapperConfig config) {
		super(config);
	}
	
	@Override
	protected TlsOutput doExecute(TlsInput input, State state, ExecutionContext context) {
		BasicMessageSender messageSender = new BasicMessageSender();
		TlsOutput output = super.doExecute(input, state, context, messageSender);
		return output;
	}

	class BasicMessageSender implements MessageSender {
		@Override
		public void sendMessage(TlsMessage message, State state, ExecutionContext context) {
			SendAction sendAction = new SendAction(message);
			sendAction.execute(state);
			if (!sendAction.isExecuted()) {
				try {
					state.getTlsContext().getTransportHandler().closeConnection();
				} catch (IOException E) {
					E.printStackTrace();
				}
			}	
		}
	} 
}
