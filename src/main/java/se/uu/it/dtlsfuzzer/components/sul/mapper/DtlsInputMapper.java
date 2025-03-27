package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.InputMapper;
import de.rub.nds.tlsattacker.core.layer.LayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.LayerStack;
import de.rub.nds.tlsattacker.core.layer.ProtocolLayer;
import de.rub.nds.tlsattacker.core.layer.SpecificSendLayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.constant.ImplementedLayers;
import de.rub.nds.tlsattacker.core.layer.impl.MessageLayer;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class DtlsInputMapper extends InputMapper<TlsInput, TlsOutput, TlsProtocolMessage, TlsExecutionContext> {

    private static final Logger LOGGER = LogManager.getLogger();

    public DtlsInputMapper(MapperConfig mapperConfig, TlsOutputChecker outputChecker) {
        super(mapperConfig, outputChecker);
    }

    @Override
    protected void sendMessage(TlsProtocolMessage message, TlsExecutionContext context) {
        ProtocolMessage protocolMessage = message.getMessage();
        State state = context.getState().getState();

        // resetting protocol layers and creating a new configuration at each layer
        LayerStack stack = state.getTlsContext().getLayerStack();
        for (ProtocolLayer<?, ?> layer : stack.getLayerList()) {
            layer.clear();
            layer.setLayerConfiguration(new SpecificSendLayerConfiguration<>(layer.getLayerType(), Collections.emptyList()));
        }

        // setting a new send configuration at the Message Layer for the message we wish to send
        MessageLayer messageLayer = (MessageLayer) state.getTlsContext().getLayerStack().getLayer(MessageLayer.class);
        LayerConfiguration<ProtocolMessage> configuration = new SpecificSendLayerConfiguration<>(ImplementedLayers.MESSAGE, Arrays.asList(protocolMessage));
        messageLayer.setLayerConfiguration(configuration);

        // performing the actual send of the message
        try {
            messageLayer.sendConfiguration();
        } catch (IOException e) {
            LOGGER.error("Failed to send message {}", protocolMessage.toCompactString());
        }

        // updating the execution context with the 'containers' that were produced at each layer
        context.getStepContext().updateSend(state);
    }
}
