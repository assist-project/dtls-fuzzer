package se.uu.it.dtlsfuzzer.mapper;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
/**
 * Comprises the result of packing a list of messages into records.
 * 
 * Limitations of our setup restricts us to sending one record per message.
 */
// FIXME fragments cannot be split over multiple records
public class PackingResult {
	private List<ProtocolMessage> messages;
	private List<AbstractRecord> records;
	public PackingResult(List<ProtocolMessage> messages,
			List<AbstractRecord> records) {
		super();
		this.messages = messages;
		this.records = records;
		if (messages.isEmpty() || records.isEmpty()) {
			throw new RuntimeException("Records and messages cannot be empty");
		}
	}
	public List<ProtocolMessage> getMessages() {
		return messages;
	}
	public List<AbstractRecord> getRecords() {
		return records;
	}
}
