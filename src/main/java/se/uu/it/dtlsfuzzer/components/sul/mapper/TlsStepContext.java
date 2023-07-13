package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.StepContext;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.Record;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;


public class TlsStepContext extends StepContext {
    private ProcessingUnit unit;
    private List<TlsMessage> receivedMessages;
    private List<Record> receivedRecords;
    private List<Pair<TlsMessage, Record>> receivedMessageRecordPairs;
    /**
     * Controls whether a flight of records ready to be sent, are done so in separate datagrams
     */
    private boolean sendRecordsIndividually;

    public TlsStepContext(int index) {
        super(index);
        sendRecordsIndividually = true;
    }

    public List<TlsMessage> getReceivedMessages() {
        return receivedMessages;
    }
    public List<Record> getReceivedRecords() {
        return receivedRecords;
    }
    public List<Pair<TlsMessage, Record>> getReceivedMessageRecordPairs() {
        return receivedMessageRecordPairs;
    }

    public void receiveUpdate(TlsMessageResponse response) {
        this.receivedRecords = response.getRecords();
        this.receivedMessages = response.getMessages();
        pairReceivedMessagesWithRecords();
    }

    private void pairReceivedMessagesWithRecords() {
        receivedMessageRecordPairs = new ArrayList<Pair<TlsMessage, Record>>();

        if (receivedMessages.size() > receivedRecords.size()) {
            if (receivedRecords.size() == 1) {
                receivedMessageRecordPairs.add(new ImmutablePair<TlsMessage, Record>(receivedMessages.get(0), receivedRecords.get(0)));
            }
            else if (receivedRecords.size() > 1) {
                int msgIndex = 0;
                int recIndex = 0;
                int msgSize = receivedMessages.size();
                int recSize = receivedRecords.size();
                TlsMessage message = receivedMessages.get(msgIndex);
                while (msgSize - msgIndex > recSize - recIndex  && recIndex < recSize) {
                    while (!(message instanceof AlertMessage) && msgSize - msgIndex >= recSize - recIndex && msgIndex < msgSize - 1) {
                        receivedMessageRecordPairs.add(new ImmutablePair<TlsMessage, Record>(message, receivedRecords.get(recIndex)));
                        msgIndex++;
                        recIndex++;
                        message = receivedMessages.get(msgIndex);
                    }
                    TlsMessage alertMessage = receivedMessages.get(msgIndex);
                    while (message instanceof AlertMessage && msgSize - msgIndex >= recSize - recIndex && msgIndex < msgSize - 1) {
                        msgIndex++;
                        message = receivedMessages.get(msgIndex);
                    }
                    receivedMessageRecordPairs.add(new ImmutablePair<TlsMessage, Record>(alertMessage, receivedRecords.get(recIndex)));
                    recIndex++;
                }
                while (recIndex < recSize) {
                    receivedMessageRecordPairs.add(new ImmutablePair<TlsMessage, Record>(receivedMessages.get(msgIndex), receivedRecords.get(recIndex)));
                    msgIndex++;
                    recIndex++;
                }
            }
        }
        else {
            Iterator<Record> itRecords = receivedRecords.iterator();
            for (TlsMessage message : receivedMessages) {
                receivedMessageRecordPairs.add(new ImmutablePair<TlsMessage, Record>(message, itRecords.next()));
            }
        }
    }

    public TlsInput getInput() {
        return (TlsInput) input;
    }

    public ProcessingUnit getProcessingUnit() {
        return unit;
    }

    public void setProcessingUnit(ProcessingUnit unit) {
        this.unit = unit;
    }

    public boolean isSendRecordsIndividually() {
        return sendRecordsIndividually;
    }

    public void setSendRecordsIndividually(boolean sendRecordsIndividually) {
        this.sendRecordsIndividually = sendRecordsIndividually;
    }
}
