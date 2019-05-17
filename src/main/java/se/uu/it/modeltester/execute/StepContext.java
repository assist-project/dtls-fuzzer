package se.uu.it.modeltester.execute;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;

public class StepContext {
	private FragmentationResult fragmentationResult;
	private PackingResult packingResult;
	private List<ProtocolMessage> receivedOutputMessages;
	
	public StepContext() {
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
	
}
