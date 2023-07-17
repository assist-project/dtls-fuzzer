package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.Record;
import java.util.List;

/**
 * Comprises the result of packing a list of messages into records.
 *
 * Limitations of our setup restricts us to sending one record per message.
 */
// FIXME fragments cannot be split over multiple records
public final class PackingResult {
    private List<ProtocolMessage<? extends ProtocolMessage<?>>> messages;
    private List<Record> records;
    public PackingResult(List<ProtocolMessage<? extends ProtocolMessage<?>>> messages, List<Record> records) {
        super();
        this.messages = messages;
        this.records = records;
        if (messages.isEmpty() || records.isEmpty()) {
            throw new RuntimeException("Records and messages cannot be empty");
        }
    }
    public List<ProtocolMessage<? extends ProtocolMessage<?>>> getMessages() {
        return messages;
    }
    public List<Record> getRecords() {
        return records;
    }
}
