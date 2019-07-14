package se.uu.it.dtlsfuzzer.mutate.record;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.execute.PackingResult;
import se.uu.it.dtlsfuzzer.mutate.Mutation;
import se.uu.it.dtlsfuzzer.mutate.PackingMutator;

public class RecordSwapMutator extends PackingMutator {

	@Override
	public Mutation<PackingResult> generateMutation(PackingResult result,
			TlsContext context, ExecutionContext exContext) {
		return null;
	}

}
