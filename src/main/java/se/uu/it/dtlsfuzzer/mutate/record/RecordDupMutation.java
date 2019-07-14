package se.uu.it.dtlsfuzzer.mutate.record;

import java.util.ArrayList;
import java.util.List;

import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.execute.PackingResult;
import se.uu.it.dtlsfuzzer.mutate.Mutation;
import se.uu.it.dtlsfuzzer.mutate.MutationType;

public class RecordDupMutation implements Mutation<PackingResult> {

	private int index;

	public RecordDupMutation(int index) {
		this.index = index;
	}

	@Override
	public PackingResult mutate(PackingResult result, TlsContext context,
			ExecutionContext exContext) {
		List<AbstractRecord> rSent = new ArrayList<>(exContext.getRecordsSent());
		rSent.addAll(result.getRecords());
		AbstractRecord recToDup = rSent.get(index >= 0
				? index
				: (rSent.size() + index));
		List<AbstractRecord> rToSend = new ArrayList<>(result.getRecords());
		rToSend.add(recToDup);
		PackingResult pResult = new PackingResult(result.getMessages(), rToSend);
		return pResult;
	}

	@Override
	public MutationType getType() {
		// TODO Auto-generated method stub
		return MutationType.RECORD_DUPLICATE;
	}

}
