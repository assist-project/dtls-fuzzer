package se.uu.it.modeltester.execute.mutate;

import de.rub.nds.tlsattacker.core.state.TlsContext;

public interface Mutator<R> {
	public R mutate(R result, TlsContext context);
	public MutatorType getType();
}
