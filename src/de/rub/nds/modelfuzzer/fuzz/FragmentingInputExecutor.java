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

	public FragmentingInputExecutor(DtlsMessageFragmenter fragmenter) {
		this.dtlsMessageFragmenter = fragmenter;
	}

	protected void sendMessage(ProtocolMessage message, State state) {
		if (message.isHandshakeMessage()) {
			stripFields(message);
			message.getHandler(state.getTlsContext()).prepareMessage(message);
			SendMessageHelper helper = new SendMessageHelper();
			List<DtlsHandshakeMessageFragment> dtlsFragments = dtlsMessageFragmenter.generateDtlsFragments((HandshakeMessage) message, state);
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
