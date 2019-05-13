package se.uu.it.modeltester.mutate;

import de.rub.nds.tlsattacker.core.state.TlsContext;

public interface Mutation<R> {
	
	public R mutate(R result, TlsContext context);
	
	public MutationType getType();
}
