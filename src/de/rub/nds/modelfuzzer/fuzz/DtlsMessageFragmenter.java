package de.rub.nds.modelfuzzer.fuzz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.serializer.HandshakeMessageSerializer;
import de.rub.nds.tlsattacker.core.state.State;

public class DtlsMessageFragmenter {
	
	private FragmentationStrategy strategy;
	private int numFragments;

	public DtlsMessageFragmenter(FragmentationStrategy strategy, int numFragments) {
		this.strategy = strategy;
		this.numFragments = numFragments;
	}

	public List<DtlsHandshakeMessageFragment> generateDtlsFragments(HandshakeMessage message, State state) {
		List<DtlsHandshakeMessageFragment> dtlsFragments = generateDtlsFragments(message, state, numFragments);
		return dtlsFragments;
	} 
	
	public List<DtlsHandshakeMessageFragment> generateDtlsFragments(HandshakeMessage message, State state, int numFrags) {
    	List<DtlsHandshakeMessageFragment> dtlsFragments = new ArrayList<>();
    	List<byte []> fragments = fragment(message, state, numFrags);
    	int fragmentOffset = 0;
    	for (byte [] fragment : fragments) {
    		DtlsHandshakeMessageFragment dtlsFragment = new DtlsHandshakeMessageFragment(message.getHandshakeMessageType(), fragment);
    		dtlsFragment.setType(message.getType());
    		dtlsFragment.setContent(fragment);
    		dtlsFragment.setLength(message.getLength().getValue());
    		dtlsFragment.setFragmentLength(fragment.length);
    		dtlsFragment.setFragmentOffset(fragmentOffset);
    		dtlsFragment.setMessageSeq((int) state.getTlsContext().getMessageSequenceNumber());
    		dtlsFragment.setCompleteResultingMessage(dtlsFragment.getHandler(state.getTlsContext()).getSerializer(dtlsFragment).serialize());
    		dtlsFragments.add(dtlsFragment);
    		fragmentOffset += fragment.length;
    	}
    	return dtlsFragments;
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
    		dtlsFragment.setMessageSeq((int) state.getTlsContext().getMessageSequenceNumber());
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
    
	private List<byte []> fragment(HandshakeMessage message, State state, int parts) {
    	List<byte []> fragments = new ArrayList<>();
    	
    	HandshakeMessageSerializer serializer = 
    			(HandshakeMessageSerializer)message
    			.getHandler(state.getTlsContext())
    			.getSerializer(message);
    	
    	byte[] content = serializer
    			.serializeHandshakeMessageContent();
    	
    	int nonEmpty = parts;
    	
    	if (content.length < nonEmpty) {
    		nonEmpty = content.length;
    	}
    	
    	switch(strategy) {
    	case EVEN:
    		fragmentEvenly(content, nonEmpty, fragments);
    		break;
    	case RANDOM:
    		fragmentRandomly(content, nonEmpty, fragments);
    	}
    	
    	for (int i=0; i<parts-nonEmpty; i++) {
    		fragments.add(new byte[] {});
    	}
    	
    	return fragments;
    }
	
	
	private void fragmentEvenly(byte [] content, int parts, List<byte []> fragments) {
		int step = content.length/parts;
    	
    	for (int i=0; i<content.length; i+=step) {
    		byte [] fragment = Arrays.copyOfRange(content, i, Math.min(i+step, content.length));
    		fragments.add(fragment);
    	}
	}
	
	private void fragmentRandomly(byte [] content, int parts, List<byte []> fragments) {
		if (parts == 0) 
			return;
		int[]  selected = IntStream.range(1, content.length-1).unordered().limit(parts-1).toArray();
		Arrays.sort(selected);
		int first=0;
		for (int i=0; i<selected.length; i++) {
			byte [] fragment = Arrays.copyOfRange(content, first, selected[i]);
			fragments.add(fragment);
			first = selected[i];
		}
		byte [] fragment = Arrays.copyOfRange(content, first, content.length);
		fragments.add(fragment);
	}
}
