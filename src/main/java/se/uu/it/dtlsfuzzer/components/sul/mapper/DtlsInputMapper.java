package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.InputMapper;
import de.rub.nds.tlsattacker.core.layer.LayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.LayerStack;
import de.rub.nds.tlsattacker.core.layer.ProtocolLayer;
import de.rub.nds.tlsattacker.core.layer.SpecificSendLayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.constant.ImplementedLayers;
import de.rub.nds.tlsattacker.core.layer.data.DataContainer;
import de.rub.nds.tlsattacker.core.layer.impl.MessageLayer;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DtlsInputMapper extends InputMapper {

    private static final Logger LOGGER = LogManager.getLogger();

    public DtlsInputMapper(MapperConfig mapperConfig, AbstractOutputChecker outputChecker) {
        super(mapperConfig, outputChecker);
    }

    protected void sendMessage(
            com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage message,
            ExecutionContext context) {
        ProtocolMessage<? extends ProtocolMessage<?>> protocolMessage = ((TlsProtocolMessage) message).getMessage();
        State state = ((TlsState) context.getState()).getState();
        LayerStack stack = state.getTlsContext().getLayerStack();
        for (ProtocolLayer layer : stack.getLayerList()) {
            layer.clear();
            layer.setLayerConfiguration(new SpecificSendLayerConfiguration<DataContainer>(layer.getLayerType(), Collections.emptyList()));
        }
        MessageLayer messageLayer = (MessageLayer) state.getTlsContext().getLayerStack().getLayer(MessageLayer.class);
        LayerConfiguration<ProtocolMessage> configuration = new SpecificSendLayerConfiguration(ImplementedLayers.MESSAGE, Arrays.asList(protocolMessage));
        messageLayer.setLayerConfiguration(configuration);
        try {
            messageLayer.sendConfiguration();
        } catch (IOException e) {
            LOGGER.error("Failed to send message {}", protocolMessage.toCompactString());
        }
    }

//
//	@Override
//	protected void sendMessage(
//			com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage message,
//			ExecutionContext context) {
//		ProtocolMessage<? extends ProtocolMessage<?>> protocolMessage = ((TlsProtocolMessage) message).getMessage();
//		State state = ((TlsState) context.getState()).getState();
//		byte[] bytes;
//		try {
//			bytes = generateMessageBytesAdjustContext(protocolMessage, state);
//			sendMessageBytes(bytes, state, protocolMessage.getProtocolMessageType());
//		} catch (IOException e) {
//			context.disableExecution();
//			LOGGER.info("Error sending message to SUT");
//		}
//	}
//
//	public <PM extends ProtocolMessage<PM>> void sendMessage(ProtocolMessage<PM> message, State state)
//			throws IOException {
//		byte[] bytes = generateMessageBytesAdjustContext(message, state);
//		sendMessageBytes(bytes, state, message.getProtocolMessageType());
//		if (message.getAdjustContext()) {
//			((ProtocolMessageHandler) message.getHandler(state.getTlsContext())).adjustContextAfterSerialize(message);
//		}
//	}
//
//	private void sendMessageBytes(byte[] bytes, State state, ProtocolMessageType type) throws IOException {
//		DtlsFragmentLayer dtlsLayer = (DtlsFragmentLayer) state.getTlsContext().getLayerStack()
//				.getLayer(DtlsFragmentLayer.class);
//		SpecificSendLayerConfiguration<DataContainer<DtlsHandshakeMessageFragment, TlsContext>> fragmentLayerConfig = new SpecificSendLayerConfiguration<>(
//				ImplementedLayers.DTLS_FRAGMENT, Collections.emptyList());
//		dtlsLayer.setLayerConfiguration(fragmentLayerConfig);
//		RecordLayer recordLayer = (RecordLayer) state.getTlsContext().getLayerStack().getLayer(RecordLayer.class);
//		SpecificSendLayerConfiguration<DataContainer<Record, TlsContext>> recordLayerConfig = new SpecificSendLayerConfiguration<>(
//				ImplementedLayers.RECORD, Collections.emptyList());
//		RecordLayerHint recordLayerHint = new RecordLayerHint(type);
//		recordLayer.setLayerConfiguration(recordLayerConfig);
//		dtlsLayer.sendData(recordLayerHint, bytes);
//	}
//
//	private final <T extends ProtocolMessage<?>> byte[] generateMessageBytesAdjustContext(ProtocolMessage<T> message,
//			State state) throws IOException {
//		ProtocolMessagePreparator<? extends ProtocolMessage<?>> preparator = message
//				.getPreparator(state.getTlsContext());
//		preparator.prepare();
//		preparator.afterPrepare();
//		ProtocolMessageSerializer<? extends ProtocolMessage<?>> serializer = message
//				.getSerializer(state.getTlsContext());
//		byte[] completeMessage = serializer.serialize();
//		message.setCompleteResultingMessage(completeMessage);
//		ProtocolMessageHandler handler = message.getHandler(state.getTlsContext());
//		handler.updateDigest(message, true);
//		if (message.getAdjustContext()) {
//			handler.adjustContext(message);
//		}
//		message.setCompleteResultingMessage(completeMessage);
//		return completeMessage;
//	}
}
