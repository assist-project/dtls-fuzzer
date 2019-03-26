package de.rub.nds.modelfuzzer.fuzz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.serializer.HandshakeMessageSerializer;
import de.rub.nds.tlsattacker.core.state.State;

public class DtlsMessageFragmenter {
	
	private int numFragments;

	public DtlsMessageFragmenter(int numFragments) {
		this.numFragments = numFragments;
	}
	
	public DtlsFragmentationResult generateDtlsFragments(HandshakeMessage message, State state, FragmentationGenerator generator) {
		byte [] content = serializeMessage(message, state);
		Fragmentation fragmentation = generator.generateFragmentation(numFragments, content.length);
		List<byte[]> fragmentedContent = fragmentContent(content, fragmentation);
		List<DtlsHandshakeMessageFragment> dtlsFragments = generateDtlsFragments(fragmentedContent, message, state);
		return new DtlsFragmentationResult(dtlsFragments, fragmentation);
	}
	
	private byte [] serializeMessage(HandshakeMessage message, State state) {

    	HandshakeMessageSerializer serializer = 
    			(HandshakeMessageSerializer)message
    			.getHandler(state.getTlsContext())
    			.getSerializer(message);

    	byte[] content = serializer
    			.serializeHandshakeMessageContent();
    	
    	return content;
	}
	
	
	private List<DtlsHandshakeMessageFragment> generateDtlsFragments(List<byte []> fragments, HandshakeMessage message, State state) {
		List<DtlsHandshakeMessageFragment> dtlsFragments = new ArrayList<>();
		int fragmentOffset = 0;
    	for (byte [] fragment : fragments) {
    		DtlsHandshakeMessageFragment dtlsFragment = new DtlsHandshakeMessageFragment(message.getHandshakeMessageType(), fragment);
    		dtlsFragment.setType(message.getType());
    		dtlsFragment.setContent(fragment);
    		dtlsFragment.setLength(message.getLength().getValue());
    		dtlsFragment.setFragmentLength(fragment.length);
    		dtlsFragment.setFragmentOffset(fragmentOffset);
    		dtlsFragment.setMessageSeq((int) state.getTlsContext().getDtlsMessageSequenceNumber());
    		dtlsFragment.setCompleteResultingMessage(dtlsFragment.getHandler(state.getTlsContext()).getSerializer(dtlsFragment).serialize());
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
