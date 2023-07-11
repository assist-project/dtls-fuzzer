package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class StepContext {
    private int index;
    private ProcessingUnit unit;
    private List<TlsMessage> receivedMessages;
    private List<AbstractRecord> receivedRecords;
    private List<Pair<TlsMessage, AbstractRecord>> receivedMessageRecordPair;
    private TlsInput input;

    /**
     * A boolean for disabling current execution.
     */
    private boolean disabled;

    /**
     * Controls whether a flight of records ready to be sent, are done so in separate datagrams
     */
    private boolean sendRecordsIndividually;

    public StepContext(int index) {
        disabled = false;
        sendRecordsIndividually = true;
        this.index = index;
    }

    public List<TlsMessage> getReceivedMessages() {
        return receivedMessages;
    }
    public List<AbstractRecord> getReceivedRecords() {
        return receivedRecords;
    }
    public List<Pair<TlsMessage, AbstractRecord>> getReceivedMessageRecordPair() {
        return receivedMessageRecordPair;
    }

    public void setReceivedRecords(List<AbstractRecord> receivedRecords) {
        this.receivedRecords = receivedRecords;
    }

    public void setReceivedMessages(List<TlsMessage> receivedOutputs) {
        this.receivedMessages = receivedOutputs;
    }

    public void pairReceivedMessagesWithRecords() {
        receivedMessageRecordPair = new ArrayList<Pair<TlsMessage, AbstractRecord>>();

        if (receivedMessages.size() > receivedRecords.size()) {
            if (receivedRecords.size() == 1) {
                receivedMessageRecordPair.add(new ImmutablePair<TlsMessage, AbstractRecord>(receivedMessages.get(0), receivedRecords.get(0)));
            }
            else if (receivedRecords.size() > 1) {
                int msgIndex = 0;
                int recIndex = 0;
                int msgSize = receivedMessages.size();
                int recSize = receivedRecords.size();
                TlsMessage message = receivedMessages.get(msgIndex);
                while (msgSize - msgIndex > recSize - recIndex  && recIndex < recSize) {
                    while (!(message instanceof AlertMessage) && msgSize - msgIndex >= recSize - recIndex && msgIndex < msgSize - 1) {
                        receivedMessageRecordPair.add(new ImmutablePair<TlsMessage, AbstractRecord>(message, receivedRecords.get(recIndex)));
                        msgIndex++;
                        recIndex++;
                        message = receivedMessages.get(msgIndex);
                    }
                    TlsMessage alertMessage = receivedMessages.get(msgIndex);
                    while (message instanceof AlertMessage && msgSize - msgIndex >= recSize - recIndex && msgIndex < msgSize - 1) {
                        msgIndex++;
                        message = receivedMessages.get(msgIndex);
                    }
                    receivedMessageRecordPair.add(new ImmutablePair<TlsMessage, AbstractRecord>(alertMessage, receivedRecords.get(recIndex)));
                    recIndex++;
                }
                while (recIndex < recSize) {
                    receivedMessageRecordPair.add(new ImmutablePair<TlsMessage, AbstractRecord>(receivedMessages.get(msgIndex), receivedRecords.get(recIndex)));
                    msgIndex++;
                    recIndex++;
                }
            }
        }
        else {
            Iterator<AbstractRecord> itRecords = receivedRecords.iterator();
            for (TlsMessage message : receivedMessages) {
                receivedMessageRecordPair.add(new ImmutablePair<TlsMessage, AbstractRecord>(message, itRecords.next()));
            }
        }
    }

    public TlsInput getInput() {
        return input;
    }

    public void setInput(TlsInput input) {
        this.input = input;
    }

    public ProcessingUnit getProcessingUnit() {
        return unit;
    }

    public void setProcessingUnit(ProcessingUnit unit) {
        this.unit = unit;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void disable() {
        disabled = true;
    }

    public boolean isSendRecordsIndividually() {
        return sendRecordsIndividually;
    }

    public void setSendRecordsIndividually(boolean sendRecordsIndividually) {
        this.sendRecordsIndividually = sendRecordsIndividually;
    }

    public int getIndex() {
        return index;
    }
}
