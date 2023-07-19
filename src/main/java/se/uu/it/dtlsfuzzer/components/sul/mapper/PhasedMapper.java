package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.beust.jcommander.internal.Lists;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;
import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputMapper;

/**
 * The mapper used to execute abstract inputs on the SUL, and capture its response in abstract outputs.
 * The mapper maintains a context of the execution in which it stores messages/fragments/records exchanged.
 * This information is useful when executing subsequent inputs.
 *
 * The mapper performs execution in phases in order to facilitate modifying input execution.
 * Changes in each phase can be implemented by just sub-classing and overriding the executePhase method.
 */
public class PhasedMapper extends MapperComposer {
    private static final Logger LOGGER = LogManager.getLogger();

    private ExecuteInputHelper helper;

    public PhasedMapper(MapperConfig config) {
        super(null, new TlsOutputMapper(config));
        helper = new ExecuteInputHelper();
    }

    @Override
    public TlsOutputChecker getAbstractOutputChecker() {
        return new TlsOutputChecker();
    }

    @Override
    protected AbstractOutput doExecute(AbstractInput input, ExecutionContext context) {
        TlsExecutionContext tlsContext = (TlsExecutionContext) context;
        ProcessingUnit unit = new ProcessingUnit();

        tlsContext.getStepContext().setProcessingUnit(unit);
        unit.setInput((TlsInput) input);

        for (Phase currentPhase : Phase.values()) {
            executePhase(currentPhase, unit, tlsContext);
        }

        AbstractOutput output = unit.getOutput();
        return output;
    }

    protected void executePhase(Phase currentPhase, ProcessingUnit unit, TlsExecutionContext context) {
        State state = context.getState().getState();

        switch (currentPhase) {
        case MESSAGE_GENERATION:
            unit.getInput().preSendUpdate(context);
            List<ProtocolMessage<? extends ProtocolMessage<?>>> messages = Arrays.asList(unit.getInput().generateProtocolMessage(context).getMessage());
            unit.setMessages(messages);
            break;

        case MESSAGE_PREPARATION:
            for (ProtocolMessage<? extends ProtocolMessage<?>> message : unit.getMessages()) {
                helper.prepareMessage(message, state);
            }
            break;

        case FRAGMENT_GENERATION:
            // We assume this is DTLS
            List<FragmentationResult> results = new ArrayList<>();
            List<ProtocolMessage<? extends ProtocolMessage<?>>> messagesToPack = new ArrayList<>();
            for (ProtocolMessage<? extends ProtocolMessage<?>> message : unit.getMessages()) {
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
            List<PackingResult> packingResults = new ArrayList<>();
            if (!unit.getMessagesToPack().isEmpty()) {
                ProtocolMessageType msgType = unit.getMessagesToPack().get(0).getProtocolMessageType();
                List<ProtocolMessage<? extends ProtocolMessage<?>>> messagesInRecord = new ArrayList<>();

                for (ProtocolMessage<? extends ProtocolMessage<?>> message : unit.getMessagesToPack()) {
                    if (msgType != message.getProtocolMessageType()) {
                        Record record = new Record(context.getConfig());
                        packingResults.add(new PackingResult(messagesInRecord, Arrays.asList(record)));
                        messagesInRecord = new ArrayList<>();
                    }
                    messagesInRecord.add(message);
                }
                packingResults.add(new PackingResult(messagesInRecord,
                        Arrays.asList(new Record(context.getConfig()))));
            }
            unit.setMessageRecords(packingResults);
            unit.setRecordsToSend(packingResults.stream().map(fr -> fr.getRecords()).flatMap(l -> l.stream())
                    .map(r -> (Record) r).collect(Collectors.toList()));
            unit.setInitialRecordsToSend(Lists.newArrayList(unit.getRecordsToSend()));
            break;

        case RECORD_PREPARATION:
            for (PackingResult messageRecord : unit.getMessageRecords()) {
                ProtocolMessageType messageType = messageRecord.getMessages().get(0).getProtocolMessageType();
                Record record = messageRecord.getRecords().get(0);
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    for (ProtocolMessage<? extends ProtocolMessage<?>> message : messageRecord.getMessages()) {
                        outputStream.write(message.getCompleteResultingMessage().getValue());
                    }
                    byte data[] = outputStream.toByteArray();
                    state.getTlsContext().getRecordLayer().prepareRecords(data, messageType,
                            Collections.singletonList(record));
                    for (ProtocolMessage<? extends ProtocolMessage<?>> message : messageRecord.getMessages()) {
                        TlsMessageHandler<ProtocolMessage<? extends ProtocolMessage<?>>> tlsMessageHandler = message.getHandler(state.getTlsContext());
                        tlsMessageHandler.adjustTlsContextAfterSerialize(message);
                    }

                } catch (IOException e) {
                    LOGGER.error("IOException in record preparation case of PhasedMapper.executePhase()");
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
            unit.getInput().postSendUpdate(context);
            break;

        case OUTPUT_RECEIVE:
            AbstractOutput output = getOutputMapper().receiveOutput(context);
            unit.setOutput(output);
            unit.getInput().postReceiveUpdate(output, getAbstractOutputChecker(), context);
            break;
        }
    }
}
