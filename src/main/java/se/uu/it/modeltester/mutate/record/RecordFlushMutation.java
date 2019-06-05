package se.uu.it.modeltester.mutate.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.PackingResult;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;

public class RecordFlushMutation implements Mutation<PackingResult>{

	@Override
	public PackingResult mutate(PackingResult result, TlsContext context, ExecutionContext exContext) {
		List<AbstractRecord> deferredRecords = 
				exContext.getStepContexes().stream().map(s -> s.getDeferredRecords()).flatMap(recs -> recs.stream()).collect(Collectors.toList());
		exContext.getStepContexes().stream().forEach(s -> s.setDeferredRecords(Collections.emptyList()));
		List<AbstractRecord> sentRecords = new ArrayList<>(deferredRecords.size() + result.getRecords().size());
		sentRecords.addAll(result.getRecords());
		sentRecords.addAll(deferredRecords);
		return new PackingResult(result.getMessages(), sentRecords);
	}

	@Override
	public MutationType getType() {
		return MutationType.RECORD_FLUSHING;
	}

}
