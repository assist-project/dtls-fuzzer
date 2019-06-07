package se.uu.it.modeltester.execute;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
/**
 * Comprises the result of packing a list of messages into records.
 */
public class PackingResult {
	private List<ProtocolMessage> messages;
	private List<AbstractRecord> records;
	public PackingResult(List<ProtocolMessage> messages,
			List<AbstractRecord> records) {
		super();
		this.messages = messages;
		this.records = records;
	}
	public List<ProtocolMessage> getMessages() {
		return messages;
	}
	public List<AbstractRecord> getRecords() {
		return records;
	}
}
