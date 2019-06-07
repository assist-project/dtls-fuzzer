package se.uu.it.modeltester.mutate.record;

import se.uu.it.modeltester.mutate.Helper;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;

import java.util.List;

import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.PackingResult;

public class RecordSwapMutation implements Mutation<PackingResult> {
	private Integer[] mapping;

	public RecordSwapMutation(Integer[] mapping) {
		this.mapping = mapping;
	}

	@Override
	public PackingResult mutate(PackingResult result, TlsContext context,
			ExecutionContext exContext) {
		List<AbstractRecord> records = result.getRecords();
		List<AbstractRecord> newRecords = Helper.reorder(records, mapping);
		return new PackingResult(result.getMessages(), newRecords);
	}

	@Override
	public MutationType getType() {
		return MutationType.RECORD_REORDERING;
	}

}
