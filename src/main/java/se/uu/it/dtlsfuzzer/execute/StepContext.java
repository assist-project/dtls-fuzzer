package se.uu.it.dtlsfuzzer.execute;

import java.util.Collections;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;

public class StepContext {
	private FragmentationResult fragmentationResult;
	private PackingResult packingResult;
	private List<ProtocolMessage> receivedOutputMessages;

	private List<AbstractRecord> deferredRecords;
	private TlsInput input;

	/**
	 * A boolean for disabling current execution.
	 */
	private boolean disabled;

	public StepContext() {
		deferredRecords = Collections.emptyList();
		disabled = false;
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

	public TlsInput getInput() {
		return this.input;
	}

	public void setInput(TlsInput input) {
		this.input = input;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void disable() {
		disabled = true;
	}
}
