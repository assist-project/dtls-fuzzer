package se.uu.it.modeltester.mutate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.serializer.HandshakeMessageSerializer;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;

public class HandshakeMessageFragmenter {
	
	private int numFragments;

	public HandshakeMessageFragmenter(int numFragments) {
		this.numFragments = numFragments;
	}
	
	public DtlsFragmentationResult generateDtlsFragments(HandshakeMessage message, State state, FragmentationGenerator generator) {
		byte [] content = serializeMessage(message, state.getTlsContext());
		Fragmentation fragmentation = generator.generateFragmentation(numFragments, content.length);
		List<byte[]> fragmentedContent = fragmentContent(content, fragmentation);
		List<DtlsHandshakeMessageFragment> dtlsFragments = generateDtlsFragments(fragmentedContent, message, state.getTlsContext());
		return new DtlsFragmentationResult(dtlsFragments, fragmentation);
	}
	
	public List<DtlsHandshakeMessageFragment> generateDtlsFragments(Fragmentation fragmentation, HandshakeMessage message, TlsContext context) {
		byte [] content = serializeMessage(message, context);
		List<byte[]> fragmentedContent = fragmentContent(content, fragmentation);
		List<DtlsHandshakeMessageFragment> dtlsFragments = generateDtlsFragments(fragmentedContent, message, context);
		return dtlsFragments;
	}
	
	
	private byte [] serializeMessage(HandshakeMessage message, TlsContext context) {

    	HandshakeMessageSerializer serializer = 
    			(HandshakeMessageSerializer)message
    			.getHandler(context)
    			.getSerializer(message);

    	byte[] content = serializer
    			.serializeHandshakeMessageContent();
    	
    	return content;
	}
	
	
	private List<DtlsHandshakeMessageFragment> generateDtlsFragments(List<byte []> fragments, HandshakeMessage message, TlsContext context) {
		List<DtlsHandshakeMessageFragment> dtlsFragments = new ArrayList<>();
		int fragmentOffset = 0;
    	for (byte [] fragment : fragments) {
    		DtlsHandshakeMessageFragment dtlsFragment = new DtlsHandshakeMessageFragment(message.getHandshakeMessageType(), fragment);
    		dtlsFragment.getHandler(context).prepareMessage(dtlsFragment);
    		dtlsFragment.setLength(message.getLength().getValue());
    		dtlsFragment.setFragmentLength(fragment.length);
    		dtlsFragment.setFragmentOffset(fragmentOffset);
    		dtlsFragment.setCompleteResultingMessage(dtlsFragment.getHandler(context).getSerializer(dtlsFragment).serialize());
    		dtlsFragments.add(dtlsFragment);
    		fragmentOffset += fragment.length;
    	}
    	return dtlsFragments;
		
	}
	
	private List<byte []> fragmentContent(byte [] content, Fragmentation fragmentation) {
		List<byte []> fragments = new ArrayList<>();

		for (Fragment fragment : fragmentation.getFragments()) {
			fragments.add(
					Arrays.copyOfRange(content, 
					fragment.getOffset(), 
					fragment.getOffset()+fragment.getLength())
					);
		}
		
		return fragments;
	}
}
