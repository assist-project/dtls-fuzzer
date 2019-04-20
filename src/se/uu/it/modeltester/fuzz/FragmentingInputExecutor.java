package se.uu.it.modeltester.fuzz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.preparator.Preparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.Serializer;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.executor.SendMessageHelper;
import se.uu.it.modeltester.sut.InputExecutor;

public class FragmentingInputExecutor extends InputExecutor {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private DtlsMessageFragmenter dtlsMessageFragmenter;
	private FragmentationGenerator fragmentationGenerator;
	
	private DtlsMessageFragmenter oneFragmentFragmenter = new DtlsMessageFragmenter(1);

	public FragmentingInputExecutor(DtlsMessageFragmenter fragmenter, FragmentationGenerator fragmentationGenerator) {
		this.dtlsMessageFragmenter = fragmenter;
		this.fragmentationGenerator = fragmentationGenerator;
	}

	protected void sendMessage(ProtocolMessage genericMessage, State state) {
		if (genericMessage.isHandshakeMessage()) {
			HandshakeMessage message = (HandshakeMessage) genericMessage;
			stripFields(message);
			
			prepareMessage(message, state);
			
			DtlsFragmentationResult result = dtlsMessageFragmenter.generateDtlsFragments((HandshakeMessage) message, state, fragmentationGenerator);
			List<DtlsHandshakeMessageFragment> dtlsFragments = result.getDtlsFragments();
			
			adjustContext(message, state);
			
			SendMessageHelper helper = new SendMessageHelper();
			List<AbstractRecord> records = generateRecords(dtlsFragments, state);
			try {
				helper.sendRecords(records, state.getTlsContext());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			super.sendMessage(genericMessage, state);
		}
	}
	
	/*
	 * Instantiates the message without changing the context. 
	 */
	private void prepareMessage(ProtocolMessage message, State state) {
		Preparator preparator = message.getHandler(state.getTlsContext()).getPreparator(message);
        preparator.prepare();
        preparator.afterPrepare();
        Serializer serializer = message.getHandler(state.getTlsContext()).getSerializer(message);
        byte[] completeMessage = serializer.serialize();
        message.setCompleteResultingMessage(completeMessage);
	}
	
	/*
	 * Makes all the necessary changes to the context
	 */
	private void adjustContext(HandshakeMessage message, State state) {
		DtlsHandshakeMessageFragment asOneFragment = oneFragmentFragmenter.generateDtlsFragments((HandshakeMessage) message, state, fragmentationGenerator).getDtlsFragments().get(0);
		message.getHandler(state.getTlsContext()).adjustTLSContext(message);
		if (message.getIncludeInDigest()) {
			LOGGER.error("Digested " + asOneFragment.toCompactString());
            LOGGER.error("Byte Array : " + ArrayConverter.bytesToHexString(asOneFragment.getCompleteResultingMessage().getValue()));
			state.getTlsContext().getDigest().append(asOneFragment.getCompleteResultingMessage().getValue());
		}
	}
	
	private List<AbstractRecord> generateRecords(List<? extends ProtocolMessage> messages, State state) {
    	List<AbstractRecord> records = new ArrayList<>();
        for (ProtocolMessage message : messages) {
        	Record rec = new Record();
        	byte [] msgBytes = message.getHandler(state.getTlsContext()).getSerializer(message).serialize();
        	state.getTlsContext().getRecordLayer().prepareRecords(msgBytes, 
        			message.getProtocolMessageType(), Arrays.asList(rec));
        	records.add(rec);
        }
        
        return records;
    }
}
