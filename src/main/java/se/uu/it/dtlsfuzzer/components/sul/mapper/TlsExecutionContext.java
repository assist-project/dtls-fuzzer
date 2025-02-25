package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContextStepped;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.layer.context.TlsContext;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.Record;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;

public class TlsExecutionContext extends ExecutionContextStepped {

    private Integer renegotiationIndex = 0;
    private Long writeRecordNumberEpoch0 = null;
    private TlsSulConfig tlsSulConfig;

    public TlsExecutionContext(TlsSulConfig tlsSulConfig, TlsState state) {
        super(state);
        this.tlsSulConfig = tlsSulConfig;
    }

    @Override
    public TlsState getState() {
        return (TlsState) state;
    }

    public TlsContext getTlsContext() {
        return ((TlsState) state).getTlsContext();
    }

    public TlsSulConfig getTlsSulConfig() {
        return tlsSulConfig;
    }

    public Config getConfig() {
        return getState().getState().getConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addStepContext() {
        stepContexts.add(new TlsStepContext(stepContexts.size()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TlsStepContext getStepContext() {
        return (TlsStepContext) super.getStepContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TlsStepContext getStepContext(int index) {
        return (TlsStepContext) super.getStepContext(index);
    }

    /**
     * Provides a fresh ordered Stream of TlsStepContext elements.
     */
    public Stream<TlsStepContext> getTlsStepContextStream() {
        return stepContexts.stream().map(step -> (TlsStepContext) step);
    }

    public List<Record> getAllRecords() {
        List<Record> records = new ArrayList<>();
        getTlsStepContextStream().forEach(tlsStep -> {
            if (tlsStep.getSentRecords() != null) {
                records.addAll(tlsStep.getSentRecords());
            }
            if (tlsStep.getReceivedRecords() != null) {
                records.addAll(tlsStep.getReceivedRecords());
            }
        });
        return records;
    }

    public List<Record> getSentRecords() {
        return getTlsStepContextStream().filter(s -> s.getSentRecords() != null)
                .flatMap(s -> s.getReceivedRecords().stream()).collect(Collectors.toList());
    }

    public List<ProtocolMessage> getReceivedMessages() {
        return getTlsStepContextStream().filter(s -> s.getReceivedMessages() != null)
                .flatMap(s -> s.getReceivedMessages().stream()).collect(Collectors.toList());
    }

    private List<Pair<ProtocolMessage, Record>> getReceivedMessagesAndRecords(Integer startingIndex) {
        return getTlsStepContextStream().skip(startingIndex)
                .filter(s -> s.getReceivedMessageRecordPairs() != null)
                .flatMap(s -> s.getReceivedMessageRecordPairs().stream()).collect(Collectors.toList());
    }

    public List<Pair<ProtocolMessage, Record>> getReceivedMessagesAndRecords() {
        return getReceivedMessagesAndRecords(0);
    }

    public List<Pair<ProtocolMessage, Record>> getHandshakeReceivedMessagesAndRecords() {
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

    /*
      public Long incrementWriteRecordNumberEpoch0() {
        Long old = writeRecordNumberEpoch0;
        writeRecordNumberEpoch0++;
        return old;
      }
    */
}
