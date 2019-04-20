package se.uu.it.modeltester.execute;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;

/**
 * Comprises the result of fragmenting a prepared message into fragments.
 */
public class FragmentationResult {
	private final HandshakeMessage message;
	private List<DtlsHandshakeMessageFragment> fragments;
	
	public FragmentationResult(HandshakeMessage message, List<DtlsHandshakeMessageFragment> fragments) {
		super();
		this.message = message;
		this.fragments = fragments;
	}

	public List<DtlsHandshakeMessageFragment> getFragments() {
		return fragments;
	}
	
	public HandshakeMessage getMessage() {
		return message;
	}

}
