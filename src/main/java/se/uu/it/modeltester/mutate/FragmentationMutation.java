package se.uu.it.modeltester.mutate;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.FragmentationResult;

/**
 * Mutates the message by applying a given fragmentation.
 */
public class FragmentationMutation implements Mutation<FragmentationResult>{
	
	private Fragmentation fragmentation;
	private HandshakeMessageFragmenter dtlsMessageFragmenter;

	public FragmentationMutation(Fragmentation fragmentation) {
		this.fragmentation = fragmentation;
		this.dtlsMessageFragmenter = new HandshakeMessageFragmenter(0);
	}

	@Override
	public FragmentationResult mutate(FragmentationResult result, TlsContext context) {
		List<DtlsHandshakeMessageFragment> newDtlsFragments = dtlsMessageFragmenter.generateDtlsFragments(fragmentation, result.getMessage(), context);
		return new FragmentationResult(result.getMessage(), newDtlsFragments);
	}

}
