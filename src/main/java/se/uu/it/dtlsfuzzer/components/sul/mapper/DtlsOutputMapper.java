package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapper;
import de.rub.nds.protocol.constants.SignatureAlgorithm;
import de.rub.nds.tlsattacker.core.layer.GenericReceiveLayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.LayerConfiguration;
import de.rub.nds.tlsattacker.core.layer.ProtocolLayer;
import de.rub.nds.tlsattacker.core.layer.context.TlsContext;
import de.rub.nds.tlsattacker.core.layer.impl.MessageLayer;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.UnknownMessage;
import de.rub.nds.x509attacker.x509.model.X509Certificate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilder;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class DtlsOutputMapper extends OutputMapper<TlsOutput, TlsProtocolMessage, TlsExecutionContext> {
    public static final int MIN_ALERTS_IN_DECRYPTION_FAILURE = 3;

    public DtlsOutputMapper(MapperConfig mapperConfig, TlsOutputBuilder outputBuilder, TlsOutputChecker outputChecker) {
        super(mapperConfig, outputBuilder, outputChecker);
    }

    @Override
    public TlsOutput receiveOutput(TlsExecutionContext context) {
        TlsContext tlsContext = context.getTlsContext();
        try {
            if (tlsContext.getTransportHandler().isClosed()) {
                return socketClosed();
            }
        } catch (IOException ex) {
            return socketClosed();
        }
        tlsContext.setTalkingConnectionEndType(tlsContext.getChooser().getMyConnectionPeer());

        // resetting protocol stack layers and creating configurations for each layer
        List<LayerConfiguration<?>> layerConfigs = new ArrayList<>(tlsContext.getLayerStack().getLayerList().size());
        for (ProtocolLayer<?, ?> layer : tlsContext.getLayerStack().getLayerList()) {
            layer.clear();
            GenericReceiveLayerConfiguration receiveConfig = new GenericReceiveLayerConfiguration(layer.getLayerType());
            layerConfigs.add(receiveConfig);
        }

        // receiving data at the Message Layer
        tlsContext.getLayerStack().receiveData(layerConfigs);
        MessageLayer messageLayer = (MessageLayer) tlsContext.getLayerStack().getLayer(MessageLayer.class);
        List<ProtocolMessage> messages = new ArrayList<>(messageLayer.getLayerResult().getUsedContainers().size());
        messageLayer.getLayerResult().getUsedContainers().stream().forEach(m -> messages.add(m));

        TlsOutput output = extractOutput(messages);
        // updating the execution context with the 'containers' that were produced at each layer
        context.getStepContext().updateReceive(context.getState().getState());
        return output;
    }

    private TlsOutput extractOutput(List<ProtocolMessage> receivedMessages) {
        if (isResponseUnknown(receivedMessages)) {
            return outputBuilder.buildUnknown();
        }
        if (receivedMessages.isEmpty()) {
            return timeout();
        } else {
            List<ProtocolMessage> tlsMessages = receivedMessages.stream().collect(Collectors.toList());
            List<String> abstractMessageStrings = extractAbstractMessageStrings(tlsMessages);
            String abstractOutput = toAbstractOutputString(abstractMessageStrings);
            List<TlsProtocolMessage> tlsProtocolMessages =
                tlsMessages.stream().map(m -> new TlsProtocolMessage(m)).collect(Collectors.toList());

            return new TlsOutput(abstractOutput, tlsProtocolMessages);
        }
    }

    private boolean isResponseUnknown(List<ProtocolMessage> receivedMessages) {
        if (receivedMessages.size() >= MIN_ALERTS_IN_DECRYPTION_FAILURE) {
            return receivedMessages.stream().allMatch(m -> m instanceof AlertMessage || m instanceof UnknownMessage);
        }
        return false;
    }

    /*
     * Failure to decrypt shows up as a longer sequence of alarm messages.
     */
    private int unknownResponseLookahed(int currentIndex, List<ProtocolMessage> messages) {
        int nextIndex = currentIndex;

        ProtocolMessage message = messages.get(nextIndex);
        while ((message instanceof AlertMessage || message instanceof UnknownMessage) && nextIndex < messages.size()) {
            message = messages.get(nextIndex);
            nextIndex++;
        }
        if (nextIndex - currentIndex >= MIN_ALERTS_IN_DECRYPTION_FAILURE)
            return nextIndex;
        return -1;
    }

    private List<String> extractAbstractMessageStrings(List<ProtocolMessage> receivedMessages) {
        List<String> outputStrings = new ArrayList<>(receivedMessages.size());
        for (int i = 0; i < receivedMessages.size(); i++) {
            // checking for cases of decryption failures, which which case
            // we add an unknown message
            int nextIndex = unknownResponseLookahed(i, receivedMessages);
            if (nextIndex > 0) {
                outputStrings.add(outputBuilder.buildUnknown().getName());
                i = nextIndex;
                if (i == receivedMessages.size()) {
                    break;
                }
            }

            ProtocolMessage m = receivedMessages.get(i);
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

    private String toOutputString(ProtocolMessage message) {
        if (message instanceof CertificateMessage cert) {
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
        SignatureAlgorithm certType;
        if (message.getCertificateEntryList() == null || message.getCertificateEntryList().isEmpty()) {
            throw new NotImplementedException("Raw public keys not supported");
        } else {
            X509Certificate x509Cert = message.getX509CertificateListFromEntries().get(0);
            certType = x509Cert.getX509SignatureAlgorithm().getSignatureAlgorithm();
        }
        return switch (certType) {
            case RSA_PKCS1, RSA_PSS_RSAE, RSA_SSA_PSS -> "RSA";
            case ECDSA -> "ECDSA";
            default ->
                throw new NotImplementedException("Signature algorithm mapping not implemented for: " + certType.name());
        };
    }

    @Override
    protected TlsOutput buildOutput(String name, List<TlsProtocolMessage> messages) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildOutput'");
    }

}
