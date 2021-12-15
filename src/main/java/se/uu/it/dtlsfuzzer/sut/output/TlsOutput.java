package se.uu.it.dtlsfuzzer.sut.output;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import se.uu.it.dtlsfuzzer.sut.Symbol;

/**
 * The outputs used in learning can be messages/records, application data, and
 * information about the state of the SUT (is it still alive).
 * 
 * We restrict ourselves only to the received message types.
 */
public class TlsOutput extends Symbol {
	private static Map<String, TlsOutput> specialOutputsMap = new HashMap<>();
	private static TlsOutput getSpecialOutput(String outString) {
		if (!specialOutputsMap.containsKey(outString))
			specialOutputsMap.put(outString, new TlsOutput(outString));
		return specialOutputsMap.get(outString);
	}
	
	public static final String MESSAGE_SEPARATOR = "|";
	public static final String REPEATING_INDICATOR = "+";

	/**
	 * Special output abstractions separator d.
	 */
	public static final String TIMEOUT = "TIMEOUT";
	public static final String UNKNOWN_MESSAGE = "UNKNOWN_MESSAGE";
	public static final String SOCKET_CLOSED = "SOCKET_CLOSED";
	public static final String DISABLED = "DISABLED";
	

	public static TlsOutput timeout() {
		return getSpecialOutput(TIMEOUT);
	}

	public static TlsOutput unknown() {
		return getSpecialOutput(UNKNOWN_MESSAGE);
	}

	public static TlsOutput socketClosed() {
		return getSpecialOutput(SOCKET_CLOSED);
	}

	public static TlsOutput disabled() {
		return getSpecialOutput(DISABLED);
	}
	
	// fields not used in equals, but as carriers of information as tests are executed
	
	// alive indicates whether the process/connection is alive or was lost
	private boolean alive = true;

	// concrete list of messages
	private List<ProtocolMessage> messages;

	public TlsOutput(String name) {
		super(name, false);
	}

	public TlsOutput(String name, List<ProtocolMessage> messages) {
		super(name, false);
		this.messages = messages;
	}
	
	/**
	 * Only includes the abstract output
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name());
		return builder.toString();
	}

	/**
	 * Includes the output header and output details
	 */
	public String toDetailedString() {
		// CLIENT_HELLO{isAlive=...}
		StringBuilder builder = new StringBuilder();
		builder.append(name());
		String contentInfo = buildContentInfo();
		builder.append(contentInfo);
		return builder.toString();
	}

	
	/**
	 * Identifies whether the output was derived from multiple distinct messages.
	 */
	public boolean isComposite() {
		return name().contains(MESSAGE_SEPARATOR);
	}
	
	/**
	 * Identifies whether the output was not derived from multiple distinct messages.
	 * This means the output encodes a single message, repeating occurrences of the same message or no message.
	 */
	public boolean isAtomic() {
		return !isComposite();
	}
	
	public boolean isRepeating() {
		return !isComposite() && name().endsWith(REPEATING_INDICATOR);
	}
	
	public TlsOutput getRepeatedOutput() {
		if (isRepeating()) {
			return new TlsOutput(name().substring(0, name().length()-1));
		}
		return this;
	}
	
	public boolean isTimeout() {
		return TIMEOUT.equals(name());
	}
	
	public boolean isDisabled() {
		return DISABLED.equals(name());
	}
	
	public boolean isSocketClosed() {
		return SOCKET_CLOSED.equals(name());
	}
	
	/**
	 * Indicates whether the output represents a record response from the system.
	 * False means the output is describes timeout/crash/disabled-ness.
	 */
	public boolean isRecordResponse() {
		return (messages != null && !messages.isEmpty()) || (!this.isTimeout() && !this.isDisabled());
	}
	
	/**
	 * Indicates whether the output also contains the concrete messages from which the abstraction was derived
	 */
	public boolean hasMessages() {
		return messages != null;
	}
	
	public List<TlsOutput> getAtomicOutputs() {
		return getAtomicOutputs(1);
	}
	
	public List<TlsOutput> getAtomicOutputs(int unrollRepeating) {
		if (isAtomic() && !isRepeating()) {
			return Collections.singletonList(this);
		} else {
			List<TlsOutput> outputs = new LinkedList<>();
			for (String absOutput : getAtomicAbstractionStrings(unrollRepeating)) {
				TlsOutput output = new TlsOutput(absOutput);
				outputs.add(output);
			}
			return outputs;
		}
	}
	
	public List<String> getAtomicAbstractionStrings() {
		return getAtomicAbstractionStrings(1);
	}
	
	/*
	 * Returns a list of abstraction strings, one for each individual message in the output, unrolling repeating messages the given number of times.
	 */
	public List<String> getAtomicAbstractionStrings(int unrollRepeating) {
		List<String> atoms =  Arrays.asList(name().split("\\"+MESSAGE_SEPARATOR));
		List<String> newAtoms = new LinkedList<>();
		for (String atom : atoms) {
			if (atom.endsWith(REPEATING_INDICATOR)) {
				String repeatingAtom = atom.substring(0, atom.length() - REPEATING_INDICATOR.length());
				IntStream.range(0, unrollRepeating)
				.forEach(i -> newAtoms.add(repeatingAtom));
			} else {
				newAtoms.add(atom);
			}
		}
		return newAtoms;
	}
	
	private String buildContentInfo() {
		StringBuilder builder = new StringBuilder();

		LinkedHashMap<String, String> printMap = new LinkedHashMap<>();
		printMap.put("isAlive", Boolean.toString(isAlive()));
		if (messages != null && !messages.isEmpty()) {
			printMap.put("messages", messages.toString());
		}

		builder.append("{");
		for (Map.Entry<String, String> entry : printMap.entrySet()) {
			builder.append(entry.getKey());
			builder.append("=");
			builder.append(entry.getValue());
			builder.append(";");
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Returns the protocol messages associated with the output. 
	 * Returns null if this output was generated from a specification and does not contain protocol messages.
	 */
	public List<ProtocolMessage> getMessages() {
		return messages;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			TlsOutput that = (TlsOutput) obj;
			// TODO not the proper way of comparing outputs but whatever
			return Objects.equals(name(), that.name());
		}
		return false;
	}

	public int hashCode() {
		int hashCode = 2 * name().hashCode(); // + (alive ? 1 : 0);
		return hashCode;
	}

}
