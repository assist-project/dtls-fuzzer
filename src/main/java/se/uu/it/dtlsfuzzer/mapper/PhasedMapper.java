package se.uu.it.dtlsfuzzer.mapper;

import com.beust.jcommander.internal.Lists;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.protocol.handler.TlsMessageHandler;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

/**
 * A mapper which implements the regular/non-altered way of sending messages.
 *
 * It performs phased execution of inputs, allowing intervention at every phase
 * by just sub-classing and overriding the executePhase method.
 */
public class PhasedMapper extends AbstractMapper {

    private ExecuteInputHelper helper;

    public PhasedMapper(MapperConfig config) {
        super(config);
        helper = new ExecuteInputHelper();
    }

    protected TlsOutput doExecute(TlsInput input, State state, ExecutionContext context) {
        ProcessingUnit unit = new ProcessingUnit();
        context.getStepContext().setProcessingUnit(unit);
        unit.setInput(input);
        for (Phase currentPhase : Phase.values()) {
            executePhase(currentPhase, unit, state, context);
        }
        TlsOutput output = unit.getOutput();
        return output;
    }

    protected void executePhase(Phase currentPhase, ProcessingUnit unit, State state, ExecutionContext context) {
        switch (currentPhase) {

        case MESSAGE_GENERATION:
            unit.getInput().preSendUpdate(state, context);
            List<TlsMessage> messages = Arrays.asList(unit.getInput().generateMessage(state, context));
            unit.setMessages(messages);
            break;

        case MESSAGE_PREPARATION:
            for (TlsMessage message : unit.getMessages()) {
                helper.prepareMessage(message, state);
            }
            break;

        case FRAGMENT_GENERATION:
            // We assume this is DTLS
            List<FragmentationResult> results = new LinkedList<>();
            List<TlsMessage> messagesToPack = new LinkedList<>();
            for (TlsMessage message : unit.getMessages()) {
                if (message.isHandshakeMessage()) {
                    FragmentationResult result = helper.fragmentMessage((HandshakeMessage) message, state);
                    results.add(result);
                    messagesToPack.addAll(result.getFragments());
                } else {
                    messagesToPack.add(message);
                }
            }
            unit.setMessageFragments(results);
            unit.setMessagesToPack(messagesToPack);
            break;

        case RECORD_GENERATION:
            List<PackingResult> packingResults = new LinkedList<>();
            if (!unit.getMessagesToPack().isEmpty()) {
                ProtocolMessageType msgType = unit.getMessagesToPack().get(0).getProtocolMessageType();
                List<TlsMessage> messagesInRecord = new LinkedList<>();

                for (TlsMessage message : unit.getMessagesToPack()) {
                    if (msgType != message.getProtocolMessageType()) {
                        AbstractRecord record = state.getTlsContext().getRecordLayer().getFreshRecord();
                        packingResults.add(new PackingResult(messagesInRecord, Arrays.asList(record)));
                        messagesInRecord = new LinkedList<>();
                    }
                    messagesInRecord.add(message);
                }
                packingResults.add(new PackingResult(messagesInRecord,
                        Arrays.asList(state.getTlsContext().getRecordLayer().getFreshRecord())));
            }
            unit.setMessageRecords(packingResults);
            unit.setRecordsToSend(packingResults.stream().map(fr -> fr.getRecords()).flatMap(l -> l.stream())
                    .map(r -> (Record) r).collect(Collectors.toList()));
            unit.setInitialRecordsToSend(Lists.newArrayList(unit.getRecordsToSend()));
            break;

        case RECORD_PREPARATION:
            for (PackingResult messageRecord : unit.getMessageRecords()) {
                ProtocolMessageType messageType = messageRecord.getMessages().get(0).getProtocolMessageType();
                AbstractRecord record = messageRecord.getRecords().get(0);
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    for (TlsMessage message : messageRecord.getMessages()) {
                        outputStream.write(message.getCompleteResultingMessage().getValue());
                    }
                    byte data[] = outputStream.toByteArray();
                    state.getTlsContext().getRecordLayer().prepareRecords(data, messageType,
                            Collections.singletonList(record));
                    for (TlsMessage message : messageRecord.getMessages()) {
                        TlsMessageHandler<TlsMessage> tlsMessageHandler = message.getHandler(state.getTlsContext());
                        tlsMessageHandler.adjustTlsContextAfterSerialize(message);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;

        case RECORD_SENDING:
            if (context.getStepContext().isSendRecordsIndividually()) {
                unit.getRecordsToSend().forEach(r -> helper.sendRecords(Arrays.asList(r), state));
            } else {
                helper.sendRecords(new ArrayList<>(unit.getRecordsToSend()), state);
            }
            unit.setRecordsSent(unit.getRecordsToSend());
            unit.getInput().postSendUpdate(state, context);
            break;

        case OUTPUT_RECEIVE:
            TlsOutput output = getOutputMapper().receiveOutput(state, context);
            unit.setOutput(output);
            unit.getInput().postReceiveUpdate(output, state, context);
            break;
        }
    }
}
