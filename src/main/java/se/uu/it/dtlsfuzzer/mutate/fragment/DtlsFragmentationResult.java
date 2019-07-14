package se.uu.it.dtlsfuzzer.mutate.fragment;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;

public class DtlsFragmentationResult {
	private List<DtlsHandshakeMessageFragment> dtlsFragments;
	private Fragmentation fragmentation;

	public DtlsFragmentationResult(
			List<DtlsHandshakeMessageFragment> dtlsFragments,
			Fragmentation fragmentation) {
		this.dtlsFragments = dtlsFragments;
		this.fragmentation = fragmentation;
	}

	public List<DtlsHandshakeMessageFragment> getDtlsFragments() {
		return dtlsFragments;
	}

	public Fragmentation getFragmentation() {
		return fragmentation;
	}

}
