package se.uu.it.modeltester.mutate;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;

public interface Mutation<R> {

	public R mutate(R result, TlsContext context, ExecutionContext exContext);

	public MutationType getType();

}
