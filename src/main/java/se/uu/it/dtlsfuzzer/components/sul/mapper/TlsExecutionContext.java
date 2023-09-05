package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContextStepped;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public class TlsExecutionContext extends ExecutionContextStepped {

    private Integer renegotiationIndex = 0;
    private Long writeRecordNumberEpoch0 = null;
    private SulConfig delegate;
    private List<Record> deferredRecords;

    public TlsExecutionContext(SulConfig delegate, TlsState state) {
        super(state);
        deferredRecords = new ArrayList<>();
        this.delegate = delegate;
    }

    public TlsState getState() {
        return (TlsState) state;
    }

    /**
     * {@inheritDoc}
     */
    public void addStepContext() {
        stepContexts.add(new TlsStepContext(stepContexts.size()));
    }

    /**
     * {@inheritDoc}
     */
    public TlsStepContext getStepContext() {
        return (TlsStepContext) super.getStepContext();
    }

    /**
     * {@inheritDoc}
     */
    public TlsStepContext getStepContext(int index) {
        return (TlsStepContext) super.getStepContext(index);
    }

    /**
     * Provides a fresh ordered Stream of TlsStepContext elements.
     */
    public Stream<TlsStepContext> getTlsStepContextStream() {
        return stepContexts.stream().map(step -> (TlsStepContext) step);
    }

    public List<AbstractRecord> getAllRecords() {
        List<AbstractRecord> records = new LinkedList<>();
        getTlsStepContextStream().forEach(tlsStep -> {
            if (tlsStep.getProcessingUnit().getRecordsSent() != null) {
                records.addAll(tlsStep.getProcessingUnit().getRecordsSent());
            }
            if (tlsStep.getReceivedRecords() != null) {
                records.addAll(tlsStep.getReceivedRecords());
            }
        });
        return records;
    }

    public List<Record> getSentRecords() {
        return getTlsStepContextStream().filter(s -> s.getProcessingUnit().getRecordsSent() != null)
                .flatMap(s -> s.getProcessingUnit().getRecordsSent().stream()).collect(Collectors.toList());
    }

    public List<TlsMessage> getReceivedMessages() {
        return getTlsStepContextStream().filter(s -> s.getReceivedMessages() != null)
                .flatMap(s -> s.getReceivedMessages().stream()).collect(Collectors.toList());
    }

    private List<Pair<TlsMessage, Record>> getReceivedMessagesAndRecords(Integer startingIndex) {
        return getTlsStepContextStream().skip(startingIndex)
                .filter(s -> s.getReceivedMessageRecordPairs() != null)
                .flatMap(s -> s.getReceivedMessageRecordPairs().stream()).collect(Collectors.toList());
    }

    public List<Pair<TlsMessage, Record>> getReceivedMessagesAndRecords() {
        return getReceivedMessagesAndRecords(0);
    }

    public List<Pair<TlsMessage, Record>> getHandshakeReceivedMessagesAndRecords() {
        return getReceivedMessagesAndRecords(renegotiationIndex);
    }

    public void updateRenegotiationIndex() {
        renegotiationIndex = stepContexts.size() - 1;
    }

    public Integer getRenegotiationIndex() {
        return renegotiationIndex;
    }

    public void setWriteRecordNumberEpoch0(Long writeRecordNumber) {
        writeRecordNumberEpoch0 = writeRecordNumber;
    }

    public Long getWriteRecordNumberEpoch0() {
        return writeRecordNumberEpoch0;
    }

    /**
      public Long incrementWriteRecordNumberEpoch0() {
        Long old = writeRecordNumberEpoch0;
        writeRecordNumberEpoch0++;
        return old;
      }
    **/

    public SulConfig getSulDelegate() {
        return delegate;
    }

    public List<Record> getDeferredRecords() {
        return new ArrayList<>(deferredRecords);
    }

    public void addDeferredRecords(Collection<Record> records) {
        deferredRecords.addAll(records);
    }

    public void clearDeferredRecords() {
        deferredRecords.clear();
    }
}
