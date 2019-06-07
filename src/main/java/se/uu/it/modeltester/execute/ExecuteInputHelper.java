package se.uu.it.modeltester.execute;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.rub.nds.tlsattacker.core.dtls.MessageFragmenter;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.executor.SendMessageHelper;

/**
 * This class is analogous to TLS-Attacker's {@link SendMessageHelper} class.
 * The difference is that it is greatly simplified, and functionality is made
 * available relevant to fuzzing fragments, specifically a method which splits
 * messages into fragments.
 * 
 */
public class ExecuteInputHelper {

	public final void prepareMessage(ProtocolMessage message, State state) {
		message.getHandler(state.getTlsContext()).prepareMessage(message);
	}

	/**
	 * Fragments prepared messages
	 */
	public final FragmentationResult fragmentMessage(HandshakeMessage message,
			State state) {
		MessageFragmenter fragmenter = new MessageFragmenter(state
				.getTlsContext().getConfig());
		List<DtlsHandshakeMessageFragment> fragments = fragmenter
				.fragmentMessage((HandshakeMessage) message,
						state.getTlsContext());
		return new FragmentationResult(message, fragments);
	}

	/**
	 * Packs messages/fragments into records.
	 */
	public final PackingResult packMessages(List<ProtocolMessage> messages,
			State state) {
		List<AbstractRecord> records = new LinkedList<>();
		for (ProtocolMessage message : messages) {
			AbstractRecord record = state.getTlsContext().getRecordLayer()
					.getFreshRecord();
			records.add(record);
			byte[] data = message.getCompleteResultingMessage().getValue();
			state.getTlsContext()
					.getRecordLayer()
					.prepareRecords(data, message.getProtocolMessageType(),
							Collections.singletonList(record));
		}
		return new PackingResult(messages, records);
	}

	public final PackingResult packMessages(List<ProtocolMessage> messages,
			TlsContext state) {
		List<AbstractRecord> records = new LinkedList<>();
		for (ProtocolMessage message : messages) {
			AbstractRecord record = state.getRecordLayer().getFreshRecord();
			records.add(record);
			byte[] data = message.getCompleteResultingMessage().getValue();
			state.getRecordLayer().prepareRecords(data,
					message.getProtocolMessageType(),
					Collections.singletonList(record));
		}
		return new PackingResult(messages, records);
	}

	/**
	 * Send records over the network.
	 */
	public final void sendRecords(List<AbstractRecord> records, State state) {
		SendMessageHelper helper = new SendMessageHelper();
		try {
			helper.sendRecords(records, state.getTlsContext());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
