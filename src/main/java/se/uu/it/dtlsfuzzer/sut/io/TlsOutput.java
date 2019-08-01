package se.uu.it.dtlsfuzzer.sut.io;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;

/**
 * The outputs used in learning comprise messages.
 */
public class TlsOutput {
	/**
	 * This is a temporary hack, should be fixed
	 */
	private static boolean compact = true;
	public static void setRepresentation(boolean compact) {
		TlsOutput.compact = compact;
	}

	public static final String TIMEOUT = "TIMEOUT";

	public static TlsOutput timeout() {
		return new TlsOutput(TIMEOUT);
	}

	public static TlsOutput socketClosed() {
		return new TlsOutput("SOCKET_CLOSED");
	}

	public static TlsOutput disabled() {
		return new TlsOutput("DISABLED");
	}

	// fields used in equals
	// the output header is the messages/records in an output
	private String outputHeader;
	// alive indicates whether the process/connection is alive or was lost
	private boolean alive = true;

	// fields not used in equals, but in the extended toString representation
	private List<ProtocolMessage> messages;
	private String applicationOutput = null;

	public TlsOutput(String messageHeader) {
		this.outputHeader = messageHeader;
	}

	public TlsOutput(String[] messageStrings) {
		this.outputHeader = buildMessageHeader(messageStrings);
		this.messages = null;
	}

	public TlsOutput(String outputHeader, List<ProtocolMessage> messages) {
		this.outputHeader = outputHeader;
		this.messages = messages;
	}

	public TlsOutput(List<ProtocolMessage> messages) {
		this.outputHeader = buildMessageHeader(messages);
		this.messages = messages;
	}

	/**
	 * Only includes the output header
	 */
	public String toString() {
		if (compact) {
			return getOutputHeader();
		} else {
			return toDetailedString();
		}
	}

	/**
	 * Includes the output header and output details
	 */
	public String toDetailedString() {
		// CLIENT_HELLO,SERVER_HELLO...
		StringBuilder builder = new StringBuilder();
		String header = getOutputHeader();
		builder.append(header);
		if (!compact) {
			builder.append("\n");
			String details = getOutputDetails();
			builder.append(details);
		}
		return builder.toString();
	}

	private String buildMessageHeader(String[] messageStrings) {
		String messageString = Arrays.stream(messageStrings)
				.reduce((s1, s2) -> s1 + "," + s2).orElse(TIMEOUT);
		return messageString;
	}

	public String buildMessageHeader(List<ProtocolMessage> messages) {
		String messageString = buildMessageHeader(messages.stream()
				.map(m -> m.toCompactString()).toArray(String[]::new));
		return messageString;
	}

	/**
	 * Compact representation of the messages.
	 */
	public String getMessageHeader() {
		return outputHeader;
	}

	/**
	 * Compact format of the output, includes the message header and
	 * not-aliveness"
	 */
	public String getOutputHeader() {
		StringBuilder builder = new StringBuilder();
		builder.append(outputHeader);
		if (!alive) {
			builder.append("[crashed]");
		}
		return builder.toString();
	}

	public String getOutputDetails() {
		StringBuilder builder = new StringBuilder();

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
		return TIMEOUT.equals(outputHeader);
	}

	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			TlsOutput that = (TlsOutput) obj;
			// TODO not the proper way of comparing outputs but whatever
			return Objects.equals(this.outputHeader, that.outputHeader)
					&& Objects.equals(this.alive, that.alive);
		}
		return false;
	}

	public int hashCode() {
		int hashCode = 2 * this.outputHeader.hashCode() + (alive ? 1 : 0);
		return hashCode;
	}

}
