package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapper;
import de.rub.nds.tlsattacker.core.layer.GenericReceiveLayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.LayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.LayerStackProcessingResult;
import de.rub.nds.tlsattacker.core.layer.ProtocolLayer;
import de.rub.nds.tlsattacker.core.layer.constant.ImplementedLayers;
import de.rub.nds.tlsattacker.core.layer.context.TlsContext;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.UnknownMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class DtlsOutputMapper extends OutputMapper {

    public DtlsOutputMapper(MapperConfig mapperConfig) {
        super(mapperConfig);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public AbstractOutput receiveOutput(ExecutionContext context) {
        TlsContext tlsContext = ((TlsExecutionContext) context).getState().getTlsContext();
        try {
            if (tlsContext.getTransportHandler().isClosed()) {
                return socketClosed();
            }
        } catch (IOException ex) {
            return socketClosed();
        }
        tlsContext.setTalkingConnectionEndType(tlsContext.getChooser().getMyConnectionPeer());

        // Resetting protocol stack layers and creating configurations for each layer
        List<LayerConfiguration> layerConfigs = new ArrayList<>(tlsContext.getLayerStack().getLayerList().size());
        for (ProtocolLayer<?,?> layer : tlsContext.getLayerStack().getLayerList()) {
            layer.clear();
            GenericReceiveLayerConfiguration receiveConfig = new GenericReceiveLayerConfiguration(layer.getLayerType());
            layerConfigs.add(receiveConfig);
        }

        // receiving data
        LayerStackProcessingResult data = tlsContext.getLayerStack().receiveData(layerConfigs);
        List<ProtocolMessage<?>> messages = data.getResultForLayer(ImplementedLayers.MESSAGE).getUsedContainers();
        AbstractOutput output = extractOutput(messages);
        // updating the execution context with the SUT response
        ((TlsExecutionContext) context).getStepContext().updateReceive(((TlsExecutionContext) context).getState().getState());
        return output;
    }

    private AbstractOutput extractOutput(List<ProtocolMessage<? extends ProtocolMessage<?>>> receivedMessages) {
        if (isResponseUnknown(receivedMessages)) {
            return AbstractOutput.unknown();
        }
        if (receivedMessages.isEmpty()) {
            return timeout();
        } else {
            List<ProtocolMessage<? extends ProtocolMessage<?>>> tlsMessages = receivedMessages.stream().collect(Collectors.toList());
            List<String> abstractMessageStrings = extractAbstractMessageStrings(tlsMessages);
            String abstractOutput = toAbstractOutputString(abstractMessageStrings);
            List<com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage> tlsProtocolMessages =
            tlsMessages.stream().map(m -> new TlsProtocolMessage(m)).collect(Collectors.toList());

            return new TlsOutput(abstractOutput, tlsProtocolMessages);
        }
    }

    private boolean isResponseUnknown(List<ProtocolMessage<? extends ProtocolMessage<?>>> receivedMessages) {
        if (receivedMessages.size() >= MIN_ALERTS_IN_DECRYPTION_FAILURE) {
            return receivedMessages.stream().allMatch(m -> m instanceof AlertMessage || m instanceof UnknownMessage);
        }
        return false;
    }

    /*
     * Failure to decrypt shows up as a longer sequence of alarm messages.
     */
    private int unknownResponseLookahed(int currentIndex, List<ProtocolMessage<? extends ProtocolMessage<?>>> messages) {
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

    private List<String> extractAbstractMessageStrings(List<ProtocolMessage<? extends ProtocolMessage<?>>> receivedMessages) {
        List<String> outputStrings = new ArrayList<>(receivedMessages.size());
        for (int i = 0; i < receivedMessages.size(); i++) {
            // checking for cases of decryption failures, which which case
            // we add an unknown message
            int nextIndex = unknownResponseLookahed(i, receivedMessages);
            if (nextIndex > 0) {
                outputStrings.add(AbstractOutput.unknown().getName());
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

    private String toOutputString(ProtocolMessage<? extends ProtocolMessage<?>> message) {
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

}
