package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapperRA;

import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.rub.nds.tlsattacker.core.layer.GenericReceiveLayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.LayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.ProtocolLayer;
import de.rub.nds.tlsattacker.core.layer.context.TlsContext;
import de.rub.nds.tlsattacker.core.layer.impl.MessageLayer;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.UnknownMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilderRA;

public class DtlsOutputMapperRA extends OutputMapperRA<PSymbolInstance, TlsProtocolMessage, TlsExecutionContextRA> {
    public static final int MIN_ALERTS_IN_DECRYPTION_FAILURE = 3;

    public DtlsOutputMapperRA(MapperConfig mapperConfig, TlsOutputBuilderRA outputBuilder) {
        super(mapperConfig, outputBuilder);
    }

    @Override
    public PSymbolInstance receiveOutput(TlsExecutionContextRA context) {
        TlsContext tlsContext = ((TlsExecutionContextRA) context).getState().getTlsContext();
        try {
            if (tlsContext.getTransportHandler().isClosed()) {
                return socketClosed();
            }
        } catch (IOException ex) {
            return socketClosed();
        }
        tlsContext.setTalkingConnectionEndType(tlsContext.getChooser().getMyConnectionPeer());

        // resetting protocol stack layers and creating configurations for each layer
        @SuppressWarnings("rawtypes")
        List<LayerConfiguration> layerConfigs = new ArrayList<>(tlsContext.getLayerStack().getLayerList().size());
        for (ProtocolLayer<?, ?> layer : tlsContext.getLayerStack().getLayerList()) {
            layer.clear();
            GenericReceiveLayerConfiguration receiveConfig = new GenericReceiveLayerConfiguration(layer.getLayerType());
            layerConfigs.add(receiveConfig);
        }

        // receiving data at the Message Layer
        tlsContext.getLayerStack().receiveData(layerConfigs);
        MessageLayer messageLayer = (MessageLayer) tlsContext.getLayerStack().getLayer(MessageLayer.class);
        List<ProtocolMessage<?>> messages = new ArrayList<>(messageLayer.getLayerResult().getUsedContainers().size());
        messageLayer.getLayerResult().getUsedContainers().stream().forEach(m -> messages.add((ProtocolMessage<?>) m));

        PSymbolInstance output = extractOutput(messages);
        // updating the execution context with the 'containers' that were produced at
        // each layer
        ((TlsExecutionContextRA) context).getStepContext()
                .updateReceive(((TlsExecutionContextRA) context).getState().getState());
        return output;
    }

    private PSymbolInstance extractOutput(List<ProtocolMessage<?>> receivedMessages) {
        if (isResponseUnknown(receivedMessages)) {
            return outputBuilder.buildUnknown();
        }
        if (receivedMessages.isEmpty()) {
            return timeout();
        } else {
            List<ProtocolMessage<? extends ProtocolMessage<?>>> tlsMessages = receivedMessages.stream()
                    .collect(Collectors.toList());
            List<String> abstractMessageStrings = extractAbstractMessageStrings(tlsMessages);
            String abstractOutput = toAbstractOutputString(abstractMessageStrings);

            // TODO: I have no idea why this is done since it is not used, because TlsOutput
            // uses Collections.emptyList().
            // List<TlsProtocolMessage> tlsProtocolMessages = tlsMessages.stream().map(m ->
            // new TlsProtocolMessage(m))
            // .collect(Collectors.toList());

            OutputSymbol baseSymbol = new OutputSymbol(abstractOutput);
            return new PSymbolInstance(baseSymbol);
        }
    }

    private boolean isResponseUnknown(List<ProtocolMessage<?>> receivedMessages) {
        if (receivedMessages.size() >= MIN_ALERTS_IN_DECRYPTION_FAILURE) {
            return receivedMessages.stream().allMatch(m -> m instanceof AlertMessage || m instanceof UnknownMessage);
        }
        return false;
    }

    /*
     * Failure to decrypt shows up as a longer sequence of alarm messages.
     */
    private int unknownResponseLookahed(int currentIndex, List<ProtocolMessage<?>> messages) {
        int nextIndex = currentIndex;

        ProtocolMessage<? extends ProtocolMessage<?>> message = messages.get(nextIndex);
        while ((message instanceof AlertMessage || message instanceof UnknownMessage) && nextIndex < messages.size()) {
            message = messages.get(nextIndex);
            nextIndex++;
        }
        if (nextIndex - currentIndex >= MIN_ALERTS_IN_DECRYPTION_FAILURE)
            return nextIndex;
        return -1;
    }

    private List<String> extractAbstractMessageStrings(List<ProtocolMessage<?>> receivedMessages) {
        List<String> outputStrings = new ArrayList<>(receivedMessages.size());
        for (int i = 0; i < receivedMessages.size(); i++) {
            // checking for cases of decryption failures, which which case
            // we add an unknown message
            int nextIndex = unknownResponseLookahed(i, receivedMessages);
            if (nextIndex > 0) {
                outputStrings.add(outputBuilder.buildUnknown().getBaseSymbol().getName());
                i = nextIndex;
                if (i == receivedMessages.size()) {
                    break;
                }
            }

            ProtocolMessage<? extends ProtocolMessage<?>> m = receivedMessages.get(i);
            String outputString = toOutputString(m);
            outputStrings.add(outputString);
        }
        return outputStrings;
    }

    private String toAbstractOutputString(List<String> abstractMessageStrings) {
        String abstractOutput = mergeRepeatingMessages(abstractMessageStrings);

        // TODO this is a hack to get learning PionDTLS server to work even when
        // retransmissions are allowed
        // PionDTLS may generate one or several HVR outputs.
        // Here we map HVR to HVR+ so that it still appears deterministic.
        if (mapperConfig.getRepeatingOutputs() != null) {
            for (String outputString : mapperConfig.getRepeatingOutputs()) {
                abstractOutput = abstractOutput.replaceAll(outputString + "\\+?", outputString + "\\+");
            }
        }

        return abstractOutput;
    }

    protected String mergeRepeatingMessages(List<String> abstractMessageStrings) {
        // in case we find repeated occurrences of types of messages, we coalesce them
        // under +,
        // since some implementations may repeat/retransmit the same message an
        // arbitrary number of times.
        StringBuilder builder = new StringBuilder();
        String lastSeen = null;
        boolean skipStar = false;

        for (String abstractMessageString : abstractMessageStrings) {
            if (lastSeen != null && lastSeen.equals(abstractMessageString) && mapperConfig.isMergeRepeating()) {
                if (!skipStar) {
                    // insert before ,
                    builder.insert(builder.length() - 1, MapperOutput.REPEATING_INDICATOR);
                    skipStar = true;
                }
            } else {
                lastSeen = abstractMessageString;
                skipStar = false;
                builder.append(lastSeen);
                builder.append(MapperOutput.MESSAGE_SEPARATOR);
            }
        }
        return builder.substring(0, builder.length() - 1);
    }

    private String toOutputString(ProtocolMessage<?> message) {
        if (message instanceof CertificateMessage) {
            CertificateMessage cert = (CertificateMessage) message;
            if (cert.getCertificatesListLength().getValue() > 0) {
                String certTypeString = getCertSignatureTypeString(cert);
                return certTypeString + "_" + message.toCompactString();
            } else {
                return "EMPTY_" + message.toCompactString();
            }
        }
        return message.toCompactString();
    }

    /*
     * Best-effort method to get the signature key type string from a non-empty
     * certificate.
     */
    private String getCertSignatureTypeString(CertificateMessage message) {
        String certType = "UNKNOWN";
        if (message.getCertificateKeyPair() == null) {
            throw new NotImplementedException("Raw public keys not supported");
        } else {
            certType = message.getCertificateKeyPair().getCertSignatureType().name();
        }
        return certType;
    }

    @Override
    protected PSymbolInstance buildOutput(String name, List<TlsProtocolMessage> messages) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildOutput'");
    }

}
