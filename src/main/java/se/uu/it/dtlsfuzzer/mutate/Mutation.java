package se.uu.it.dtlsfuzzer.mutate;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;

public interface Mutation<R> {

	public R mutate(R result, TlsContext context, ExecutionContext exContext);

	public MutationType getType();

}
