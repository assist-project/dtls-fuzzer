package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.InputMapperRA;
import de.learnlib.ralib.words.PSymbolInstance;
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
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputCheckerRA;

public class DtlsInputMapperRA
        extends InputMapperRA<PSymbolInstance, TlsProtocolMessage, TlsExecutionContextRA> {

    private static final Logger LOGGER = LogManager.getLogger();

    public DtlsInputMapperRA(MapperConfig mapperConfig, TlsOutputCheckerRA outputChecker) {
        super(mapperConfig, outputChecker);
    }

    @Override
    public void sendMessage(
            TlsProtocolMessage message,
            TlsExecutionContextRA context) {
        ProtocolMessage<? extends ProtocolMessage<?>> protocolMessage = ((TlsProtocolMessage) message).getMessage();
        State state = ((TlsState) context.getState()).getState();

        // resetting protocol layers and creating a new configuration at each layer
        LayerStack stack = state.getTlsContext().getLayerStack();
        for (ProtocolLayer<?, ?> layer : stack.getLayerList()) {
            layer.clear();
            layer.setLayerConfiguration(new SpecificSendLayerConfiguration<DataContainer<?, ?>>(layer.getLayerType(),
                    Collections.emptyList()));
        }

        // setting a new send configuration at the Message Layer for the message we wish
        // to send
        MessageLayer messageLayer = (MessageLayer) state.getTlsContext().getLayerStack().getLayer(MessageLayer.class);
        LayerConfiguration<ProtocolMessage<?>> configuration = new SpecificSendLayerConfiguration<>(
                ImplementedLayers.MESSAGE, Arrays.asList(protocolMessage));
        messageLayer.setLayerConfiguration(configuration);

        // performing the actual send of the message
        try {
            messageLayer.sendConfiguration();
        } catch (IOException e) {
            LOGGER.error("Failed to send message {}", protocolMessage.toCompactString());
        }

        // updating the execution context with the 'containers' that were produced at
        // each layer
        context.getStepContext().updateSend(state);
    }

    @Override
    public void preSendUpdate(PSymbolInstance input, TlsExecutionContextRA context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'preSendUpdate'");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(PSymbolInstance input, TlsExecutionContextRA context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateProtocolMessage'");
    }

    @Override
    public void postReceiveUpdate(PSymbolInstance input, PSymbolInstance output,
            OutputChecker<PSymbolInstance> outputChecker, TlsExecutionContextRA context) {
    }

    @Override
    public void postSendUpdate(PSymbolInstance input, TlsExecutionContextRA context) {
    }
}
