package se.uu.it.modeltester;

import net.automatalib.automata.transout.impl.FastMealy;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class DiagnosisTask {

	private FastMealy<TlsInput, TlsOutput> specification;

	DiagnosisTask(FastMealy<TlsInput, TlsOutput>  specification) {
		this.specification = specification;
	}
	
	public FastMealy<TlsInput, TlsOutput> getSpecification() {
		return specification;
	}
}
