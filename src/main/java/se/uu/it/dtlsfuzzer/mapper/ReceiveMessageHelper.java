package se.uu.it.dtlsfuzzer.mapper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.rub.nds.tlsattacker.core.exceptions.ParserException;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.executor.MessageActionResult;
import de.rub.nds.tlsattacker.core.workflow.action.executor.RecordGroup;

/**
 * A simplified ve
 */
public class ReceiveMessageHelper {
    
    private TlsContext context;

    public ReceiveMessageHelper(TlsContext context) {
        this.context = context;
    } 
    
    
//    private MessageActionResult processUngroupedRecords(List<AbstractRecord> tempRecords, TlsContext context) {
//        MessageActionResult result = new MessageActionResult();
//        List<RecordGroup> recordGroups = RecordGroup.generateRecordGroups(tempRecords);
//        for (int groupIndex = 0; groupIndex < recordGroups.size(); groupIndex++) {
//            RecordGroup currentGroup = recordGroups.get(groupIndex);
//
//            boolean foundValidRecordInGroup = false;
//            byte[] preservedDigest = context.getDigest().getRawBytes();
//            long preservedReadSQN = context.getReadSequenceNumber();
//
//            for (int recordIndex = 0; recordIndex < recordGroups.get(groupIndex).getRecords().size(); recordIndex++) {
//                currentGroup.decryptRecord(context, recordIndex);
//                currentGroup.adjustContextForRecord(context, recordIndex);
//
//                if (currentGroup.areAllRecordsValid()) {
//                    foundValidRecordInGroup = true;
//                } else if (!currentGroup.areAllRecordsValid() && foundValidRecordInGroup) {
//                    context.setReadSequenceNumber(context.getReadSequenceNumber() - 1);
//                    formNewGroupFromLastAndComingRecords(recordIndex, groupIndex, recordGroups);
//                }
//            }
//
//            try {
//                MessageActionResult tempResult = parseRecordGroup(recordGroups.get(groupIndex), context);
//                result = result.merge(tempResult);
//            } catch (ParserException parserException) {
//                List<AbstractRecord> additionalRecords = tryToFetchAdditionalRecords(context);
//                RecordGroup.mergeRecordsIntoGroups(recordGroups, additionalRecords);
//                restorePreGroupState(context, preservedDigest, preservedReadSQN);
//                groupIndex--;
//            }
//        }
//        return result;
//    }
    
//    private void formProcessableRecordGroups(Collection<Record> parsedRecords, Collection<RecordGroup> groups) {
//        group = new RecordGroup();
//        for (Record record : parsedRecords) {
//            
//        }
//    }
    
    private void parseRecords(byte [] receivedBytes, Collection<Record> parsedRecords) {
        List<AbstractRecord> records = context.getRecordLayer().parseRecords(receivedBytes);
        for (AbstractRecord record : records) {
            if (! (record instanceof Record)) {
                throw new ReceiveMessageException(String.format("Record of type %s is not supported", record.getClass()));
            }
            Record parsedRecord = (Record) record;
            parsedRecord.adjustContext(context);
            parsedRecords.add((Record) record);
            
        }
    }  
    
    private byte [] receiveBytes() {
        try {
            return context.getTransportHandler().fetchData();
        } catch (IOException exception) {
            throw new ReceiveMessageException(exception);
        }
    }
}
