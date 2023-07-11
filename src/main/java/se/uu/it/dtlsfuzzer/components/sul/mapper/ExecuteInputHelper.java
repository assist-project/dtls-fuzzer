package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.dtls.MessageFragmenter;
import de.rub.nds.tlsattacker.core.protocol.Preparator;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.Serializer;
import de.rub.nds.tlsattacker.core.protocol.handler.TlsMessageHandler;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.executor.SendMessageHelper;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is analogous to TLS-Attacker's {@link SendMessageHelper} class.
 *
 * The difference is that it is greatly simplified, and functionality is made
 * available relevant to fuzzing fragments, specifically a method which splits
 * messages into fragments.
 */
public class ExecuteInputHelper {

    /**
     * Prepares a TlsMessage; parts were taken from {@link SendMessageHelper}
     */
    public final void prepareMessage(TlsMessage message, State state) {
        TlsContext context = state.getTlsContext();
        Preparator<ProtocolMessage> preparator = message.getHandler(context).getPreparator(message);
        preparator.prepare();
        preparator.afterPrepare();
        Serializer<ProtocolMessage> serializer = message.getHandler(context).getSerializer(message);
        byte[] completeMessage = serializer.serialize();
        message.setCompleteResultingMessage(completeMessage);

        if (message.getAdjustContext()) {
            if (context.getConfig().getDefaultSelectedProtocolVersion().isDTLS()
                    && (message instanceof HandshakeMessage)
                    && !((HandshakeMessage) message).isDtlsHandshakeMessageFragment()) {
                context.increaseDtlsWriteHandshakeMessageSequence();
            }
        }

        if (message instanceof TlsMessage) {
            TlsMessageHandler<TlsMessage> handler = message.getHandler(context);
            handler.updateDigest(message);
        }
        if (message.getAdjustContext()) {
            message.getHandler(context).adjustContext(message);
        }
    }

    /**
     * Fragments prepared messages
     */
    public final FragmentationResult fragmentMessage(HandshakeMessage message, State state) {
        List<DtlsHandshakeMessageFragment> fragments = Collections
                .singletonList(MessageFragmenter.wrapInSingleFragment(message, state.getTlsContext()));
        return new FragmentationResult(message, fragments);
    }

    /**
     * Packs messages/fragments into records ready to be sent.
     */
    public final PackingResult packMessages(List<TlsMessage> messages, State state) {
        List<AbstractRecord> records = new LinkedList<>();
        for (TlsMessage message : messages) {
            AbstractRecord record = state.getTlsContext().getRecordLayer().getFreshRecord();
            records.add(record);
            byte[] data = message.getCompleteResultingMessage().getValue();
            state.getTlsContext().getRecordLayer().prepareRecords(data, message.getProtocolMessageType(),
                    Collections.singletonList(record));
        }
        return new PackingResult(messages, records);
    }

    /**
     * Sends records over the network.
     */
    public final void sendRecords(List<AbstractRecord> records, State state) {
        SendMessageHelper helper = new SendMessageHelper();
        try {
            helper.sendRecords(records, state.getTlsContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
