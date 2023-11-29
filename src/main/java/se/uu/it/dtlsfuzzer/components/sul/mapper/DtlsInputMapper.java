package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.InputMapper;
import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.layer.SpecificSendLayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.constant.ImplementedLayers;
import de.rub.nds.tlsattacker.core.layer.constant.LayerType;
import de.rub.nds.tlsattacker.core.layer.context.TlsContext;
import de.rub.nds.tlsattacker.core.layer.data.DataContainer;
import de.rub.nds.tlsattacker.core.layer.hints.RecordLayerHint;
import de.rub.nds.tlsattacker.core.layer.impl.DtlsFragmentLayer;
import de.rub.nds.tlsattacker.core.layer.impl.RecordLayer;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessageHandler;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessagePreparator;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessageSerializer;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import java.io.IOException;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DtlsInputMapper extends InputMapper {

    private static final Logger LOGGER = LogManager.getLogger();

    public DtlsInputMapper(MapperConfig mapperConfig, AbstractOutputChecker outputChecker) {
        super(mapperConfig, outputChecker);
    }

    @Override
    protected void sendMessage(com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage message, ExecutionContext context) {
        ProtocolMessage<? extends ProtocolMessage<?>> protocolMessage = ((TlsProtocolMessage) message).getMessage();
        State state = ((TlsState) context.getState()).getState();
        byte[] bytes;
        try {
            bytes = generateMessageBytesAdjustContext(protocolMessage, state);
            sendMessageBytes(bytes, state, protocolMessage.getProtocolMessageType());
        } catch (IOException e) {
            context.disableExecution();
            LOGGER.info("Error sending message to SUT");
        }
    }

    public <PM extends ProtocolMessage<PM>> void sendMessage(ProtocolMessage<PM> message, State state) throws IOException {
        byte[] bytes = generateMessageBytesAdjustContext(message, state);
        sendMessageBytes(bytes, state, message.getProtocolMessageType());
        if (message.getAdjustContext()) {
            ((ProtocolMessageHandler) message.getHandler(state.getTlsContext())).adjustContextAfterSerialize(message);
        }
    }

	private void sendMessageBytes(byte [] bytes, State state, ProtocolMessageType type) throws IOException {
        RecordLayerHint recordLayerHint = new RecordLayerHint(type);
        DtlsFragmentLayer dtlsLayer = (DtlsFragmentLayer) state.getTlsContext().getLayerStack().getLayer(DtlsFragmentLayer.class);
        LayerType dtlsLayerType = ImplementedLayers.DTLS_FRAGMENT;
        SpecificSendLayerConfiguration<DataContainer<DtlsHandshakeMessageFragment, TlsContext>> layerType = new SpecificSendLayerConfiguration<>(dtlsLayerType, Collections.emptyList());
        dtlsLayer.setLayerConfiguration(layerType);
        RecordLayer recordLayer = (RecordLayer) state.getTlsContext().getLayerStack().getLayer(RecordLayer.class);
        SpecificSendLayerConfiguration<DataContainer<Record, TlsContext>> recordLayerConfig = new SpecificSendLayerConfiguration<>(ImplementedLayers.RECORD, Collections.emptyList());
        recordLayer.setLayerConfiguration(recordLayerConfig);
        dtlsLayer.sendData(recordLayerHint, bytes);
    }

	private final byte [] generateMessageBytesAdjustContext(ProtocolMessage<? extends ProtocolMessage<?>> message, State state) throws IOException {
         ProtocolMessagePreparator<? extends ProtocolMessage<?>> preparator = message.getPreparator(state.getTlsContext());
         preparator.prepare();
         preparator.afterPrepare();
         ProtocolMessageSerializer<? extends ProtocolMessage<?>> serializer = message.getSerializer(state.getTlsContext());
         byte[] completeMessage = serializer.serialize();
         message.setCompleteResultingMessage(completeMessage);
         ProtocolMessageHandler handler = message.getHandler(state.getTlsContext());
         handler.updateDigest(message, true);
         if (message.getAdjustContext()) {
             handler.adjustContext(message);
         }
         message.setCompleteResultingMessage(completeMessage);
         return completeMessage;
     }
}
