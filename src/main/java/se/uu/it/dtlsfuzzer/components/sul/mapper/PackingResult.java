package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import java.util.List;
/**
 * Comprises the result of packing a list of messages into records.
 *
 * Limitations of our setup restricts us to sending one record per message.
 */
// FIXME fragments cannot be split over multiple records
public final class PackingResult {
    private List<TlsMessage> messages;
    private List<AbstractRecord> records;
    public PackingResult(List<TlsMessage> messages,
            List<AbstractRecord> records) {
        super();
        this.messages = messages;
        this.records = records;
        if (messages.isEmpty() || records.isEmpty()) {
            throw new RuntimeException("Records and messages cannot be empty");
        }
    }
    public List<TlsMessage> getMessages() {
        return messages;
    }
    public List<AbstractRecord> getRecords() {
        return records;
    }
}
