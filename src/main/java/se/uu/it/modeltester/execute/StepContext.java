package se.uu.it.modeltester.execute;

import java.util.Collections;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;

public class StepContext {
	private FragmentationResult fragmentationResult;
	private PackingResult packingResult;
	private List<ProtocolMessage> receivedOutputMessages;
	
	private List<AbstractRecord> deferredRecords;
	
	public StepContext() {
		deferredRecords = Collections.emptyList();
	}
	
	public FragmentationResult getFragmentationResult() {
		return fragmentationResult;
	}
	public PackingResult getPackingResult() {
		return packingResult;
	}
	public List<ProtocolMessage> getReceivedOutputMessages() {
		return receivedOutputMessages;
	}
	public void setFragmentationResult(FragmentationResult fragmentationResult) {
		this.fragmentationResult = fragmentationResult;
	}
	public void setPackingResult(PackingResult packingResult) {
		this.packingResult = packingResult;
	}
	public void setReceivedOutputMessages(List<ProtocolMessage> receivedOutputs) {
		this.receivedOutputMessages = receivedOutputs;
	}

	public List<AbstractRecord> getDeferredRecords() {
		return deferredRecords;
	}

	public void setDeferredRecords(List<AbstractRecord> deferredRecords) {
		this.deferredRecords = deferredRecords;
	}
}
