package se.uu.it.modeltester.execute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;

public class ExecutionContext {

	private List<StepContext> stepContexes;

	public ExecutionContext() {
		stepContexes = new ArrayList<>();
	}

	public void addStepContext() {
		stepContexes.add(new StepContext());
	}

	public StepContext getStepContext() {
		if (!stepContexes.isEmpty())
			return stepContexes.get(stepContexes.size() - 1);
		return null;
	}

	public List<StepContext> getStepContexes() {
		return Collections.unmodifiableList(stepContexes);
	}

	public List<DtlsHandshakeMessageFragment> getFragmentsSent() {
		return stepContexes
				.stream()
				.filter(s -> s.getFragmentationResult() != null)
				.flatMap(
						fs -> fs.getFragmentationResult().getFragments()
								.stream()).collect(Collectors.toList());
	}

	public List<ProtocolMessage> getMessagesSent() {
		return stepContexes.stream().filter(s -> s.getPackingResult() != null)
				.flatMap(s -> s.getPackingResult().getMessages().stream())
				.collect(Collectors.toList());
	}

	public List<AbstractRecord> getRecordsSent() {
		return stepContexes.stream().filter(s -> s.getPackingResult() != null)
				.flatMap(s -> s.getPackingResult().getRecords().stream())
				.collect(Collectors.toList());
	}

	public StepContext getStepContext(int ind) {
		return stepContexes.get(ind);
	}

	public int getStepCount() {
		return stepContexes.size();
	}

}
