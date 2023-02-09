package se.uu.it.dtlsfuzzer.mapper;

import java.util.List;
import java.util.stream.Collectors;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.Record;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

/**
 * All work involving execution of an input is performed on a processing unit.
 * The work is done is multiple phases, each of which involving a few fields on the class.
 * First an input is prepared for execution.
 * Then messages are generated and prepared.
 * Handshake messages are distributed in fragments.
 * Fragments are then packed in records and sent over the wire.
 */
public class ProcessingUnit {

	// the starting input
	private TlsInput input;

	// the messages is generated
	private List<TlsMessage> messages;

	// how handshake messages are distributed into fragments
	private List<FragmentationResult> messageFragments;

	// the messages ready to be packed in records
	private List<TlsMessage> messagesToPack;

	// how messages are distributed into records
	private List<PackingResult> messageRecords;

	// the records that are going to be sent in their initial configuration
	private List<Record> initialRecordsToSend;

	// the records that are going to be sent
	private List<Record> recordsToSend;

	// the records that were sent
	private List<Record> recordsSent;

	// the output that was received
	private TlsOutput output;

	public TlsInput getInput() {
		return input;
	}

	public void setInput(TlsInput input) {
		this.input = input;
	}

	public List<TlsMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<TlsMessage> messages) {
		this.messages = messages;
	}

	public void setMessageFragments(List<FragmentationResult> messageFragments) {
		this.messageFragments = messageFragments;
	}

	public List<DtlsHandshakeMessageFragment> getFragments() {
		return messageFragments.stream()
		.map(mf -> mf.getFragments())
		.flatMap(l -> l.stream())
		.collect(Collectors.toList());
	}

	public List<PackingResult> getMessageRecords() {
		return this.messageRecords;
	}

	/**
	 * automatically updates {@link ProcessingUnit#recordsToSend}
	 * @param recordsToSend
	 */
	public void setMessageRecords(List<PackingResult> messageRecords) {
		this.messageRecords = messageRecords;
	}

	public List<Record> getRecordsToSend() {
		return recordsToSend;
	}

	public void setRecordsToSend(List<Record> records) {
		this.recordsToSend = records;
	}

	public List<Record> getInitialRecordsToSend() {
		return initialRecordsToSend;
	}

	public void setInitialRecordsToSend(List<Record> records) {
		this.initialRecordsToSend = records;
	}

	public List<TlsMessage> getMessagesToPack() {
		return messagesToPack;
	}

	public void setMessagesToPack(List<TlsMessage> messagesToPack) {
		this.messagesToPack = messagesToPack;
	}


	public List<Record> getRecordsSent() {
		return recordsSent;
	}

	public void setRecordsSent(List<Record> recordsSent) {
		this.recordsSent = recordsSent;
	}

	public TlsOutput getOutput() {
		return output;
	}

	public void setOutput(TlsOutput output) {
		this.output = output;
	}

}
