package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.StepContext;
import de.rub.nds.tlsattacker.core.dtls.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.layer.impl.DtlsFragmentLayer;
import de.rub.nds.tlsattacker.core.layer.impl.MessageLayer;
import de.rub.nds.tlsattacker.core.layer.impl.RecordLayer;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class TlsStepContext extends StepContext<TlsInput, TlsOutput> {

    private List<ProtocolMessage> sentMessages;
    private List<DtlsHandshakeMessageFragment> sentFragments;
    private List<Record> sentRecords;

    private List<ProtocolMessage> receivedMessages;
    private List<DtlsHandshakeMessageFragment> receivedFragments;
    private List<Record> receivedRecords;

    public TlsStepContext(int index) {
        super(index);
    }

    /**
     * Should be called after receiving a response from the SUT.
     */
    void updateReceive(State state) {
        MessageLayer messageLayer = (MessageLayer) state.getTlsContext().getLayerStack().getLayer(MessageLayer.class);
        receivedMessages = new ArrayList<>(messageLayer.getLayerResult().getUsedContainers().size());
        messageLayer.getLayerResult().getUsedContainers().forEach(m -> receivedMessages.add(m));
        DtlsFragmentLayer fragmentLayer = state.getTlsContext().getDtlsFragmentLayer();
        receivedFragments = new ArrayList<>(fragmentLayer.getLayerResult().getUsedContainers());
        RecordLayer recordLayer = state.getTlsContext().getRecordLayer();
        receivedRecords = new ArrayList<>(recordLayer.getLayerResult().getUsedContainers());
    }

    /**
     * Should be called after executing an input on the SUT.
     */
    void updateSend(State state) {
        MessageLayer messageLayer = (MessageLayer) state.getTlsContext().getLayerStack().getLayer(MessageLayer.class);
        sentMessages = new ArrayList<>(messageLayer.getLayerResult().getUsedContainers().size());
        messageLayer.getLayerResult().getUsedContainers().forEach(m -> sentMessages.add(m));
        DtlsFragmentLayer fragmentLayer = state.getTlsContext().getDtlsFragmentLayer();
        sentFragments = new ArrayList<>(fragmentLayer.getLayerResult().getUsedContainers());
        RecordLayer recordLayer = state.getTlsContext().getRecordLayer();
        sentRecords = new ArrayList<>(recordLayer.getLayerResult().getUsedContainers());
    }

    public List<ProtocolMessage> getReceivedMessages() {
        return receivedMessages;
    }

    public List<ProtocolMessage> getSentMessages() {
        return sentMessages;
    }

    public List<DtlsHandshakeMessageFragment> getSentFragments() {
        return sentFragments;
    }

    public List<DtlsHandshakeMessageFragment> getReceivedFragments() {
        return receivedFragments;
    }

    public List<Record> getReceivedRecords() {
        return receivedRecords;
    }

    public List<Record> getSentRecords() {
        return sentRecords;
    }

    @Override
    public TlsInput getInput() {
        return input;
    }

    public List<Pair<ProtocolMessage, Record>> getReceivedMessageRecordPairs() {
        assert receivedRecords.size() == receivedMessages.size();
        return IntStream.range(0, receivedMessages.size()).boxed()
                .map(i -> Pair.<ProtocolMessage, Record>of(receivedMessages.get(i), receivedRecords.get(i)))
                .collect(Collectors.toList());
    }
}
