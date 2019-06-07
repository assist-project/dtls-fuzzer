package se.uu.it.modeltester.mutate.record;

import java.util.Collections;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.PackingResult;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;

public class RecordDeferMutation implements Mutation<PackingResult> {

	@Override
	public PackingResult mutate(PackingResult result, TlsContext context,
			ExecutionContext exContext) {
		exContext.getStepContext().setDeferredRecords(result.getRecords());
		return new PackingResult(result.getMessages(), Collections.emptyList());
	}

	@Override
	public MutationType getType() {
		return MutationType.RECORD_DEFERRAL;
	}

}
