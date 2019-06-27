package se.uu.it.modeltester.sut.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;

/**
 * The outputs used in learning comprise message strings obtained by compacting
 * messages.
 */
public class TlsOutput {
	// fields used in equals
	private List<String> messageStrings;
	private boolean alive = true;

	// fields not used in equals, but used in toString representation
	private List<ProtocolMessage> messages;
	private String applicationOutput = null;
	private boolean compact = true;

	private static final TlsOutput SOCKET_CLOSED = new TlsOutput(
			new ArrayList<>());

	public static TlsOutput socketClosed() {
		return SOCKET_CLOSED;
	}

	public TlsOutput() {
	}

	public TlsOutput(String[] messageStrings) {
		this.messageStrings = Arrays.asList(messageStrings);
		this.messages = null;
	}

	public TlsOutput(List<ProtocolMessage> messages) {
		messageStrings = messages.stream().map(m -> m.toCompactString())
				.collect(Collectors.toList());
		this.messages = messages;
	}

	public String toString() {
		if (compact) {
			return toCompactString();
		} else {
			return toFullString();
		}
	}

	public String toFullString() {
		// CLIENT_HELLO,SERVER_HELLO...
		StringBuilder builder = new StringBuilder();
		String messageString = messageStrings.stream()
				.reduce((s1, s2) -> s1 + "," + s2).orElse("TIMEOUT");
		builder.append(messageString);
		if (!alive) {
			builder.append("[crashed]");
		}
		LinkedHashMap<String, String> printMap = new LinkedHashMap<>();
		if (messages != null && !messages.isEmpty()) {
			printMap.put("messages", messages.toString());
		}
		if (applicationOutput != null) {
			printMap.put("appOutput", applicationOutput);
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

	public String toCompactString() {
		// CLIENT_HELLO,SERVER_HELLO...
		String messageString = messageStrings.stream()
				.reduce((s1, s2) -> s1 + "," + s2).orElse("TIMEOUT");

		if (!alive) {
			messageString = messageString.concat("[crashed]");
		}
		if (applicationOutput != null) {
			messageString = messageString.concat("{" + "appOutput="
					+ applicationOutput + "}");
		}
		return messageString;
	}

	/**
	 * Returns the protocol messages associated with the output. Returns null if
	 * this output was derived from a specification.
	 */
	public List<ProtocolMessage> getMessages() {
		return messages;
	}

	public void setApplicationOutput(String log) {
		this.applicationOutput = log;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isTimeout() {
		return messageStrings.isEmpty();
	}

	public void setCompact(boolean compact) {
		this.compact = compact;
	}

	public boolean isCompact() {
		return this.compact;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			TlsOutput that = (TlsOutput) obj;
			// TODO not the proper way of comparing outputs but whatever
			return Objects.equals(this.toString(), that.toString())
					&& Objects.equals(this.alive, that.alive);
		}
		return false;
	}

	public int hashCode() {
		int hashCode = 2 * toString().hashCode() + (alive ? 1 : 0);
		return hashCode;
	}

}
