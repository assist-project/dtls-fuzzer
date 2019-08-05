package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

public class IsAliveWrapper implements SUL<TlsInput, TlsOutput> {

	private SUL<TlsInput, TlsOutput> sut;
	private boolean isAlive;

	public IsAliveWrapper(SUL<TlsInput, TlsOutput> sut) {
		this.sut = sut;
	}

	@Override
	public void pre() {
		sut.pre();
		isAlive = true;
	}

	@Override
	public void post() {
		sut.post();
	}

	@Override
	public TlsOutput step(TlsInput in) throws SULException {
		if (isAlive) {
			TlsOutput out = sut.step(in);
			isAlive = out.isAlive();
			return out;
		} else {
			return TlsOutput.socketClosed();
		}
	}
}
