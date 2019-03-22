package de.rub.nds.modelfuzzer.fuzz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.executor.SendMessageHelper;

public class FragmentingInputExecutor extends InputExecutor {
	
	private DtlsMessageFragmenter dtlsMessageFragmenter;
	private FragmentationGenerator fragmentationGenerator;

	public FragmentingInputExecutor(DtlsMessageFragmenter fragmenter, FragmentationGenerator fragmentationGenerator) {
		this.dtlsMessageFragmenter = fragmenter;
		this.fragmentationGenerator = fragmentationGenerator;
	}

	protected void sendMessage(ProtocolMessage message, State state) {
		if (message.isHandshakeMessage()) {
			stripFields(message);
			message.setAdjustContext(false);
			message.getHandler(state.getTlsContext()).prepareMessage(message);
			SendMessageHelper helper = new SendMessageHelper();
			DtlsFragmentationResult result = dtlsMessageFragmenter.generateDtlsFragments((HandshakeMessage) message, state, fragmentationGenerator);
			List<DtlsHandshakeMessageFragment> dtlsFragments = result.getDtlsFragments();
			List<AbstractRecord> records = generateRecords(dtlsFragments, state);
			try {
				helper.sendRecords(records, state.getTlsContext());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			super.sendMessage(message, state);
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
