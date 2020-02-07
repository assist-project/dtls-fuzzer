package se.uu.it.dtlsfuzzer.execute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.executor.SendMessageHelper;

/**
 * An lightweight input executor which sends messages by calling the
 * sendMessages functionality on the SendMessageHelper.
 * 
 * It does not update the execution context.
 */
public class BasicInputExecutor extends AbstractInputExecutor {

	protected void sendMessage(ProtocolMessage message, State state,
			ExecutionContext context) {
		Record record = new Record();
		List<ProtocolMessage> messages = new LinkedList<>();
		List<AbstractRecord> records = new LinkedList<>();
		records.add(record);
		messages.add(message);
		SendMessageHelper helper = new SendMessageHelper();
		try {
			helper.sendMessages(messages, records, state.getTlsContext());
		} catch (IOException ex) {
			try {
				state.getTlsContext().getTransportHandler().closeConnection();
			} catch (IOException E) {
				E.printStackTrace();
			}
		}
	}
}
