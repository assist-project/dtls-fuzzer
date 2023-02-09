package se.uu.it.dtlsfuzzer.mapper;

import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import se.uu.it.dtlsfuzzer.config.SulDelegate;

public class ExecutionContext {

    private List<StepContext> stepContexes;
    private Integer renegotiationIndex = 0;
    private boolean enabled = true;
    private Long writeRecordNumberEpoch0 = null;
    private SulDelegate delegate;
    private List<Record> deferredRecords;
    private State state;

    public ExecutionContext(SulDelegate delegate, State state) {
        stepContexes = new ArrayList<>();
        this.delegate = delegate;
        deferredRecords = new ArrayList<>();
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public TlsContext getTlsContext() {
        return state.getTlsContext();
    }

    public void disableExecution() {
        enabled = false;
    }

    public void enableExecution() {
        enabled = true;
    }

    public boolean isExecutionEnabled() {
        return enabled;
    }

    public void addStepContext() {
        stepContexes.add(new StepContext(stepContexes.size()));
    }

    public StepContext getStepContext() {
        if (!stepContexes.isEmpty())
            return stepContexes.get(stepContexes.size() - 1);
        return null;
    }

    public List<StepContext> getStepContexes() {
        return Collections.unmodifiableList(stepContexes);
    }

    public List<AbstractRecord> getAllRecords() {
        List<AbstractRecord> records = new LinkedList<>();
        for (StepContext step : stepContexes) {
            if (step.getProcessingUnit().getRecordsSent() != null) {
                records.addAll(step.getProcessingUnit().getRecordsSent());
            }
            if (step.getReceivedRecords() != null) {
                records.addAll(step.getReceivedRecords());
            }
        }
        return records;
    }

    public List<Record> getSentRecords() {
        return stepContexes.stream().filter(s -> s.getProcessingUnit().getRecordsSent() != null)
                .flatMap(s -> s.getProcessingUnit().getRecordsSent().stream()).collect(Collectors.toList());
    }

    public List<TlsMessage> getReceivedMessages() {
        return stepContexes.stream().filter(s -> s.getReceivedMessages() != null)
                .flatMap(s -> s.getReceivedMessages().stream()).collect(Collectors.toList());
    }

    private List<Pair<TlsMessage, AbstractRecord>> getReceivedMessagesAndRecords(Integer startingIndex) {
        return stepContexes.subList(startingIndex, stepContexes.size()).stream()
                .filter(s -> s.getReceivedMessageRecordPair() != null)
                .flatMap(s -> s.getReceivedMessageRecordPair().stream()).collect(Collectors.toList());
    }

    public List<Pair<TlsMessage, AbstractRecord>> getReceivedMessagesAndRecords() {
        return getReceivedMessagesAndRecords(0);
    }

    public List<Pair<TlsMessage, AbstractRecord>> getHandshakeReceivedMessagesAndRecords() {
        return getReceivedMessagesAndRecords(renegotiationIndex);
    }

    public StepContext getStepContext(int ind) {
        return stepContexes.get(ind);
    }

    public int getStepCount() {
        return stepContexes.size();
    }

    public void updateRenegotiationIndex() {
        renegotiationIndex = stepContexes.size() - 1;
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

    public Long incrementWriteRecordNumberEpoch0() {
        Long old = writeRecordNumberEpoch0;
        writeRecordNumberEpoch0++;
        return old;
    }

    public SulDelegate getSulDelegate() {
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
