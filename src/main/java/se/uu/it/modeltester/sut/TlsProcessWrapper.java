package se.uu.it.modeltester.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class TlsProcessWrapper extends SulProcessWrapper<TlsInput, TlsOutput>{

	public TlsProcessWrapper(SUL<TlsInput, TlsOutput> sul, ProcessHandler handler) {
		super(sul, handler);
	}

	@Override
	public TlsOutput step(TlsInput in) throws SULException {
		TlsOutput output = super.step(in);
		output.setIsAlive(super.isAlive());
		return output;
	}
}
