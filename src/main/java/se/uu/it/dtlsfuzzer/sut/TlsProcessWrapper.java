package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import se.uu.it.dtlsfuzzer.config.SulDelegate;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class TlsProcessWrapper extends SulProcessWrapper<TlsInput, TlsOutput> {

	public TlsProcessWrapper(SUL<TlsInput, TlsOutput> sul,
			SulDelegate sulDelegate) {
		super(sul, sulDelegate);
	}

	public void pre() {
		super.pre();
	}

	@Override
	public TlsOutput step(TlsInput in) throws SULException {
		TlsOutput output = super.step(in);
		output.setAlive(super.isAlive());
		return output;
	}
}
