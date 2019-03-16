package de.rub.nds.modelfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;

/**
 * A SUL wrapper which joins the SUL with a handler for the process that launches/terminates it.
 */
public class SulProcessWrapper<I,O> implements SUL<I,O> {
	
	private SUL<I,O> sul;
	private ProcessHandler handler;

	public SulProcessWrapper(SUL<I,O> sul, ProcessHandler handler) {
		this.sul = sul;
		this.handler = handler;
	}

	@Override
	public void pre() {
		handler.launchProcess();
		sul.pre();
	}

	@Override
	public void post() {
		sul.post();
		handler.terminateProcess();
	}

	@Override
	public O step(I in) throws SULException {
		return sul.step(in);
	}
	
}
