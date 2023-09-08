package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.constants.HandshakeByteLength;
import de.rub.nds.tlsattacker.core.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.dtls.FragmentManager;
import de.rub.nds.tlsattacker.core.exceptions.AdjustmentException;
import de.rub.nds.tlsattacker.core.exceptions.ParserException;
import de.rub.nds.tlsattacker.core.protocol.ParserResult;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessageHandler;
import de.rub.nds.tlsattacker.core.protocol.handler.DtlsHandshakeMessageFragmentHandler;
import de.rub.nds.tlsattacker.core.protocol.handler.UnknownMessageHandler;
import de.rub.nds.tlsattacker.core.protocol.handler.factory.HandlerFactory;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.record.RecordCryptoComputations;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.executor.MessageActionResult;
import de.rub.nds.tlsattacker.core.workflow.action.executor.ReceiveMessageHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to receive records/messages from the SUL.
 * The code is adapted from TLS-Attacker's
 * {@link de.rub.nds.tlsattacker.core.workflow.action.executor.ReceiveMessageHelper} class.
 * Changes were made to simplify it, consistent with our assumption that messages arrive in-order and no time-based re-transmissions occur.
 */
public class TlsMessageReceiver {
    private static final Logger LOGGER = LogManager.getLogger();

    private ReceiveMessageHelper receiveHelper;

    public TlsMessageReceiver() {
        receiveHelper = new ReceiveMessageHelper();
    }

    /**
     * Receives and processes records/fragments/messages. Assumes packets are
     * delivered in order and no time-based retransmissions are present.
     */
    public TlsMessageResponse receiveMessages(TlsContext context) {
        context.setTalkingConnectionEndType(context.getChooser().getMyConnectionPeer());
        MessageActionResult result = new MessageActionResult();
        // we use the fragment manager simply as a convenient way of assembling
        // fragments
        // we do not currently use it for retransmission/reordering
        FragmentManager fragmentManager = new FragmentManager(context.getConfig());
        byte[] bytes = receiveBytes(context);
        while (bytes.length > 0) {
            List<Record> records = new ArrayList<Record>();
            parseRecords(bytes, records, context);
            MessageActionResult intermediaryResult = processParsedRecords(records, fragmentManager, context);
            result.merge(intermediaryResult);
            bytes = receiveBytes(context);
        }
        return new TlsMessageResponse(result);
    }

    /*
     * Processes a collection of parsed records to extract encapsulated fragments
     * and messages.
     */
    private MessageActionResult processParsedRecords(Collection<Record> parsedRecords, FragmentManager fragmentManager, TlsContext context) {
        // form groups of records that can be processed together, e.g. records from the
        // same epoch with the same ContentType
        List<RecordGroup> recordGroups = formRecordGroups(parsedRecords);
        MessageActionResult result = new MessageActionResult();
        for (RecordGroup group : recordGroups) {

            // decrypt and update context (e.g. readSequenceNumber) for each record in the
            // group
            for (Record record : group.getRecords()) {
                context.getRecordLayer().decryptAndDecompressRecord(record);
                record.adjustContext(context);
            }

            // in rare instances, we may not be able to decrypt or authenticate the record
            // we then separate it from other records so as to not affect their processing
            for (RecordGroup processableGroup : group.splitIntoProcessableGroups()) {
                MessageActionResult groupResult = processRecordGroup(processableGroup, fragmentManager, context);
                result.merge(groupResult);
            }
        }

        return result;
    }

    /*
     * Processes the decrypted payload of a group of records, extracting and
     * processing messages and fragments.
     */
    private MessageActionResult processRecordGroup(RecordGroup group, FragmentManager fragmentManager, TlsContext context) {
        List<ProtocolMessage> processedMessages = new ArrayList<>();
        List<DtlsHandshakeMessageFragment> processedFragments = new ArrayList<>();

        // handle decrypted payload extracting/processing messages
        List<ProtocolMessage> handledMessages = handleRecordBytes(group.getCleanProtocolMessageBytes(),
                group.getProtocolMessageType(), group.getEpoch(), context);

        // for Handshake fragments, we do another processing round, this time
        // extracting/processing handshake messages
        for (ProtocolMessage message : handledMessages) {
            if (message instanceof DtlsHandshakeMessageFragment) {
                DtlsHandshakeMessageFragment messageFragment = (DtlsHandshakeMessageFragment) message;
                processedFragments.add(messageFragment);
                fragmentManager.addMessageFragment(messageFragment);
                boolean complete = fragmentManager
                        .isFragmentedMessageComplete(messageFragment.getMessageSeq().getValue(), group.epoch);
                if (complete) {
                    DtlsHandshakeMessageFragment combinedFragment = fragmentManager
                            .getCombinedMessageFragment(messageFragment.getMessageSeq().getValue(), group.epoch);
                    ProtocolMessage handshakeMessage = handleCombinedFragment(combinedFragment, context);
                    processedMessages.add(handshakeMessage);
                }
            } else {
                processedMessages.add(message);
            }
        }

        return new MessageActionResult(group.getAbstractRecords(), processedMessages, processedFragments);
    }

    /*
     * Parse and extract Handshake message from a combined fragment.
     */
    private ProtocolMessage handleCombinedFragment(DtlsHandshakeMessageFragment combinedFragment, TlsContext context) {
        byte[] messageBytes = convertDtlsFragmentToCleanTlsBytes(combinedFragment, context);
        HandshakeMessageType handshakeMessageType = HandshakeMessageType.getMessageType(messageBytes[0]);
        ProtocolMessageHandler protocolMessageHandler = HandlerFactory.getHandler(context,
                ProtocolMessageType.HANDSHAKE, handshakeMessageType);

        // this informs TLS-Attacker of the message_seq to use for the HandshakeMessage,
        // when updating the digest
        context.setDtlsReadHandshakeMessageSequence(combinedFragment.getMessageSeq().getValue());
        ParserResult result = receiveHelper.parseMessage(protocolMessageHandler, messageBytes, 0, false, context);
        ProtocolMessage message = result.getMessage();
        if (message instanceof HandshakeMessage) {
            ((HandshakeMessage) message).setMessageSequence(combinedFragment.getMessageSeq());
        }
        return message;
    }

    /*
     * Handles decrypted record bytes.
     */
    private List<ProtocolMessage> handleRecordBytes(byte[] recordBytes, ProtocolMessageType type, int epoch,
            TlsContext context) {
        int dataPointer = 0;
        List<ProtocolMessage> receivedMessages = new ArrayList<>();

        while (dataPointer < recordBytes.length) {
            ParserResult result = null;
            switch (type) {
            case HANDSHAKE:
                try {
                    result = tryHandleAsDtlsHandshakeMessageFragments(recordBytes, dataPointer, context);
                } catch (Exception exception) {
                    // this is normally a sign that we have decrypted a record using the wrong keys, hence the message makes no sense.
                    LOGGER.debug("Could not parse Message as DtlsHandshakeMessageFragment");
                    LOGGER.debug(exception);
                    result = tryHandleAsUnknownMessage(recordBytes, dataPointer, context,
                            ProtocolMessageType.HANDSHAKE);
                }
                break;
            default:
                result = tryHandleAsCorrectMessage(recordBytes, dataPointer, type, context);
                break;
            }
            if (result != null) {
                if (dataPointer == result.getParserPosition()) {
                    throw new ParserException("Ran into an infinite loop while parsing ProtocolMessages");
                }
                dataPointer = result.getParserPosition();
                LOGGER.debug("The following message was parsed: {}", result.getMessage().toString());
                if (result.getMessage() instanceof DtlsHandshakeMessageFragment) {
                    ((DtlsHandshakeMessageFragment) result.getMessage()).setEpoch(epoch);
                }
                receivedMessages.add(result.getMessage());
            }
        }
        return receivedMessages;
    }

    /*
     * Processes a fragmented message by extracting the underlying message.
     */
    private byte[] convertDtlsFragmentToCleanTlsBytes(DtlsHandshakeMessageFragment fragment, TlsContext context) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(fragment.getType().getValue());
        try {
            stream.write(ArrayConverter.intToBytes(fragment.getLength().getValue(),
                    HandshakeByteLength.MESSAGE_LENGTH_FIELD));
            stream.write(fragment.getContent().getValue());
        } catch (IOException ex) {
            LOGGER.warn("Could not write fragment to stream.", ex);
        }
        return stream.toByteArray();
    }

    private ParserResult tryHandleAsCorrectMessage(byte[] protocolMessageBytes, int pointer,
            ProtocolMessageType typeFromRecord, TlsContext context) throws ParserException, AdjustmentException {

        HandshakeMessageType handshakeMessageType = HandshakeMessageType.getMessageType(protocolMessageBytes[pointer]);
        ProtocolMessageHandler protocolMessageHandler = HandlerFactory.getHandler(context, typeFromRecord,
                handshakeMessageType);
        return receiveHelper.parseMessage(protocolMessageHandler, protocolMessageBytes, pointer, false, context);
    }

    public ParserResult tryHandleAsDtlsHandshakeMessageFragments(byte[] recordBytes, int pointer, TlsContext context)
            throws ParserException, AdjustmentException {
        DtlsHandshakeMessageFragmentHandler dtlsHandshakeMessageHandler = new DtlsHandshakeMessageFragmentHandler(
                context);
        return receiveHelper.parseMessage(dtlsHandshakeMessageHandler, recordBytes, pointer, false, context);
    }

    private ParserResult tryHandleAsUnknownHandshakeMessage(byte[] protocolMessageBytes, int pointer,
            ProtocolMessageType typeFromRecord, TlsContext context) throws ParserException, AdjustmentException {
        ProtocolMessageHandler pmh = HandlerFactory.getHandler(context, typeFromRecord, HandshakeMessageType.UNKNOWN);
        return receiveHelper.parseMessage(pmh, protocolMessageBytes, pointer, false, context);
    }

    private ParserResult tryHandleAsUnknownMessage(byte[] protocolMessageBytes, int pointer, TlsContext context,
            ProtocolMessageType recordContentMessageType) throws ParserException, AdjustmentException {
        UnknownMessageHandler unknownHandler = new UnknownMessageHandler(context, recordContentMessageType);
        return receiveHelper.parseMessage(unknownHandler, protocolMessageBytes, pointer, false, context);
    }

    private List<RecordGroup> formRecordGroups(Collection<Record> parsedRecords) {
        List<RecordGroup> groups = new ArrayList<>(parsedRecords.size());
        Iterator<Record> iter = parsedRecords.iterator();
        if (iter.hasNext()) {
            Record record = iter.next();
            RecordGroup currentGroup = new RecordGroup(record);
            groups.add(currentGroup);
            while (iter.hasNext()) {
                record = iter.next();
                boolean added = currentGroup.addRecord(record);
                if (!added) {
                    currentGroup = new RecordGroup(record);
                    groups.add(currentGroup);
                }
            }
        }
        return groups;
    }

    private void parseRecords(byte[] receivedBytes, Collection<Record> parsedRecords, TlsContext context) {
        List<AbstractRecord> records = context.getRecordLayer().parseRecords(receivedBytes);
        for (AbstractRecord record : records) {
            if (!(record instanceof Record)) {
                throw new ReceiveMessageException(
                        String.format("Record of type %s is not supported", record.getClass()));
            }
            parsedRecords.add((Record) record);
        }
    }

    private byte[] receiveBytes(TlsContext context) {
        try {
            return context.getTransportHandler().fetchData();
        } catch (IOException exception) {
            LOGGER.warn("Received " + exception.getLocalizedMessage() + " while receiving for Messages.", exception);
            context.setReceivedTransportHandlerException(true);
            throw new ReceiveMessageException(exception);
        }
    }

    static class RecordGroup {
        private List<Record> records;
        private byte contentType;
        private int epoch;

        RecordGroup(Record record) {
            records = new ArrayList<>();
            records.add(record);
            contentType = record.getContentType().getValue();
            epoch = record.getEpoch().getValue();
        }

        RecordGroup(Collection<Record> records) {
            if (records.isEmpty()) {
                throw new InternalError("Cannot construct group from an empty collection of records");
            }
            this.records = new ArrayList<>(records.size());
            Iterator<Record> iter = records.iterator();
            Record record = iter.next();
            this.records.add(record);
            contentType = record.getContentType().getValue();
            epoch = record.getEpoch().getValue();
            while (iter.hasNext()) {
                addRecord(record);
            }
        }

        boolean addRecord(Record record) {
            if (record.getContentType().getValue() == contentType && record.getEpoch().getValue() == epoch) {
                records.add(record);
                return true;
            }
            return false;
        }

        public List<Record> getRecords() {
            return Collections.unmodifiableList(records);
        }

        public List<AbstractRecord> getAbstractRecords() {
            return Collections.unmodifiableList(records);
        }

        ProtocolMessageType getProtocolMessageType() {
            return ProtocolMessageType.getContentType(contentType);
        }

        byte getContentType() {
            return contentType;
        }

        int getEpoch() {
            return epoch;
        }

        byte[] getCleanProtocolMessageBytes() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            for (Record record : records) {
                try {
                    stream.write(record.getCleanProtocolMessageBytes().getValue());
                } catch (IOException e) {
                    throw new ReceiveMessageException("Could not write clean protocol message bytes of received record",
                            e);
                }
            }
            return stream.toByteArray();
        }

        List<RecordGroup> splitIntoProcessableGroups() {
            List<RecordGroup> groups = new ArrayList<>();
            Iterator<Record> iter = records.iterator();
            RecordGroup processableGroup = new RecordGroup(iter.next());
            groups.add(processableGroup);
            while (iter.hasNext()) {
                Record record = iter.next();
                if (isRecordInvalid(record)) {
                    processableGroup = new RecordGroup(record);
                    groups.add(processableGroup);
                } else {
                    processableGroup.addRecord(record);
                }
            }
            return groups;
        }

        boolean isRecordGroupInvalid() {
            return records.stream().anyMatch(r -> isRecordInvalid(r));
        }

        private boolean isRecordInvalid(AbstractRecord record) {
            if (record instanceof Record) {
                RecordCryptoComputations computations = ((Record) record).getComputations();
                if (computations != null && (Objects.equals(computations.getMacValid(), Boolean.FALSE)
                        || Objects.equals(computations.getPaddingValid(), Boolean.FALSE)
                        || Objects.equals(computations.getAuthenticationTagValid(), Boolean.FALSE))) {
                    return true;
                }
            }
            return false;
        }
    }
}
