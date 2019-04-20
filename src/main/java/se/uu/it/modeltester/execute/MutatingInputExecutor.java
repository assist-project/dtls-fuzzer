package se.uu.it.modeltester.execute;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rub.nds.tlsattacker.core.dtls.MessageFragmenter;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.executor.SendMessageHelper;
import se.uu.it.modeltester.execute.mutate.FragmentationMutator;
import se.uu.it.modeltester.execute.mutate.Mutator;
import se.uu.it.modeltester.execute.mutate.MutatorType;

/**
 * An input executor which can accept mutators at different points in 
 * the execution of an input.
 */
public class MutatingInputExecutor extends InputExecutor {
	private static final Logger LOGGER = LogManager.getLogger();
	private Map<MutatorType, List<Mutator<?>>> mutators;
	
	public MutatingInputExecutor() {
		super();
		mutators = new LinkedHashMap<>();
	}

	@Override
	protected void sendMessage(ProtocolMessage message, State state) {
		state.getTlsContext().setTalkingConnectionEndType(state.getTlsContext().getChooser().getConnectionEndType());
		message
		.getHandler(state.getTlsContext())
		.prepareMessage(message);
		
		List<ProtocolMessage> messagesToSend = new LinkedList<>();

		if (message.isHandshakeMessage() && state.getTlsContext().getConfig().getDefaultSelectedProtocolVersion().isDTLS()) {
			FragmentationResult result = fragmentMessage((HandshakeMessage) message, state);
			result = applyMutators(MutatorType.FRAGMENTATION, result, state);
			messagesToSend.addAll(result.getFragments());
		} else {
			messagesToSend.add(message);
		}
		
		PackingResult result = packMessages(messagesToSend, state);
		message.getHandler(state.getTlsContext()).adjustTlsContextAfterSerialize(message);
		applyMutators(MutatorType.PACKING, result, state);
		SendMessageHelper helper = new SendMessageHelper();
		List<AbstractRecord> recordsToSend = result.getRecords();
		try {
			helper.sendRecords(recordsToSend, state.getTlsContext());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fragments prepared messages
	 */
	private FragmentationResult fragmentMessage(HandshakeMessage message, State state) {
		MessageFragmenter fragmenter = new MessageFragmenter(state.getTlsContext().getConfig());
		List<DtlsHandshakeMessageFragment> fragments = fragmenter.fragmentMessage((HandshakeMessage) message,
				state.getTlsContext());
		return new FragmentationResult(message, fragments);
	}

	/**
	 * Packs messages into records.
	 */
	private PackingResult packMessages(List<ProtocolMessage> messages, State state) {
		List<AbstractRecord> records = new LinkedList<>();
		for (ProtocolMessage message : messages) {
			AbstractRecord record = state.getTlsContext().getRecordLayer().getFreshRecord();
			records.add(record);
			byte[] data = message.getCompleteResultingMessage().getValue();
			state.getTlsContext().getRecordLayer().prepareRecords(data, message.getProtocolMessageType(),
					Collections.singletonList(record));
		}
		return new PackingResult(messages, records);
	}

	private <R> R applyMutators(MutatorType mutatorType, R currentResult, State state) {
		List<Mutator<?>> mutatorsOfType = mutators.getOrDefault(mutatorType, Collections.emptyList());
		R result = currentResult;
		for (Mutator<?> mutator : mutatorsOfType) {
			// this can definitely be made safe with some more work;
			Mutator<R> castMutator = (Mutator<R>) mutator;
			result = castMutator.mutate(result, state.getTlsContext());
		}
		return result;
	}
	
	/**
	 * Adds a mutator
	 */
	public <M extends Mutator<?>> boolean addMutator(M mutator) {
		mutators.putIfAbsent(mutator.getType(), new LinkedList<>());
		List<Mutator<?>> mutOfSameType = mutators.get(mutator.getType());
		mutOfSameType.add(mutator);
		return true;
	}
}
