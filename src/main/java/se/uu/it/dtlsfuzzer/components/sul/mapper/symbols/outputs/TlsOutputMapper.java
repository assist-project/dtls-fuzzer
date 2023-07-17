package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapper;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.protocol.message.UnknownMessage;
import de.rub.nds.tlsattacker.core.state.State;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsMessageReceiver;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsMessageResponse;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsStepContext;

/**
 * The output mapper performs the following functions:
 * <ol>
 * <li>receives the SUT's response (records) over the wire;</li>
 * <li>processes the response by:</li>
 * <ul>
 * <li>updating the internal state;</li>
 * <li>converting response to a corresponding {@link TlsOutput}.</li>
 * </ul>
 * </ol>
 *
 * Everything having to do with how a response is converted into a TlsOutput
 * should be implemented here. Also implemented are operations over the mapper
 * such as coalescing to outputs into one or splitting an output into its atoms.
 */
public class TlsOutputMapper extends OutputMapper {
    private static final Logger LOGGER = LogManager.getLogger();

    public TlsOutputMapper(MapperConfig mapperConfig) {
        super(mapperConfig);
    }

    public AbstractOutput receiveOutput(ExecutionContext context) {
        TlsExecutionContext tlsContext = (TlsExecutionContext) context;
        State state = tlsContext.getState().getState();

        try {
            if (state.getTlsContext().getTransportHandler().isClosed()) {
                return socketClosed();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return socketClosed();
        }
        try {
            TlsMessageReceiver receiver = new TlsMessageReceiver();
            TlsMessageResponse response = receiver.receiveMessages(state.getTlsContext());
            TlsStepContext tlsStepContext = (TlsStepContext) tlsContext.getStepContext();
            tlsStepContext.receiveUpdate(response);

            return extractOutput(state, response.getMessages());
        } catch (Exception ex) {
            ex.printStackTrace();
            return socketClosed();
        }
    }

    private boolean isResponseUnknown(List<TlsMessage> receivedMessages) {
        if (receivedMessages.size() >= MIN_ALERTS_IN_DECRYPTION_FAILURE) {
            return receivedMessages.stream().allMatch(m -> m instanceof AlertMessage || m instanceof UnknownMessage);
        }
        return false;
    }

    /*
     * Failure to decrypt shows up as a longer sequence of alarm messages.
     */
    private int unknownResponseLookahed(int currentIndex, List<TlsMessage> messages) {
        int nextIndex = currentIndex;

        TlsMessage message = messages.get(nextIndex);
        while ((message instanceof AlertMessage || message instanceof UnknownMessage) && nextIndex < messages.size()) {
            message = messages.get(nextIndex);
            nextIndex++;
        }
        if (nextIndex - currentIndex >= MIN_ALERTS_IN_DECRYPTION_FAILURE)
            return nextIndex;
        return -1;
    }

    private AbstractOutput extractOutput(State state, List<TlsMessage> receivedMessages) {
        if (isResponseUnknown(receivedMessages)) {
            return AbstractOutput.unknown();
        }
        if (receivedMessages.isEmpty()) {
            return timeout();
        } else {
            List<TlsMessage> tlsMessages = receivedMessages.stream().map(p -> (TlsMessage) p)
                    .collect(Collectors.toList());
            List<String> abstractMessageStrings = extractAbstractMessageStrings(tlsMessages, state);
            String abstractOutput = toAbstractOutputString(abstractMessageStrings);
            List<ProtocolMessage> tlsProtocolMessages =
            tlsMessages.stream().map(m -> new TlsProtocolMessage(m)).collect(Collectors.toList());

            return new TlsOutput(abstractOutput, tlsProtocolMessages);
        }
    }

    private List<String> extractAbstractMessageStrings(List<TlsMessage> receivedMessages, State state) {
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

            TlsMessage m = receivedMessages.get(i);
            String outputString = toOutputString(m, state);
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

    private String toOutputString(TlsMessage message, State state) {
        if (message instanceof CertificateMessage) {
            CertificateMessage cert = (CertificateMessage) message;
            if (cert.getCertificatesListLength().getValue() > 0) {
                String certTypeString = getCertSignatureTypeString(cert, state);
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
    private String getCertSignatureTypeString(CertificateMessage message, State state) {
        String certType = "UNKNOWN";
        if (message.getCertificateKeyPair() == null) {
            throw new NotImplementedException("Raw public keys not supported");
        } else {
            certType = message.getCertificateKeyPair().getCertSignatureType().name();
        }
        return certType;
    }
}
