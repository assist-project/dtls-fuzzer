package se.uu.it.modeltester.mutate.record;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.PackingResult;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.PackingMutator;

public class RecordSwapMutator extends PackingMutator {

	@Override
	public Mutation<PackingResult> generateMutation(PackingResult result,
			TlsContext context, ExecutionContext exContext) {
		return null;
	}

}
