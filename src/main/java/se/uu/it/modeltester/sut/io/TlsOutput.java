package se.uu.it.modeltester.sut.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;

/**
 * The outputs used in learning comprise message strings obtained by compacting
 * messages.
 */
public class TlsOutput {
	private List<String> messageStrings;
	private List<ProtocolMessage> messages;
	private boolean isAlive = true;

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
		// CLIENT_HELLO,SERVER_HELLO...
		String messageString = messageStrings.stream()
				.reduce((s1, s2) -> s1 + "," + s2).orElse("TIMEOUT");

		if (!isAlive) {
			messageString = messageString.concat("[crashed]");
		}
		return messageString;
	}

	public void setIsAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public boolean isAlive() {
		return isAlive;
	}
	
	public boolean isTimeout() {
		return messageStrings.isEmpty();
	}
	
	/**
	 * Returns the protocol messages associated with the output. Returns null if this output was derived from a specification.
	 */
	public List<ProtocolMessage> getMessages() {
		return messages;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			TlsOutput that = (TlsOutput) obj;
			// TODO not the proper way of comparing outputs but whatever
			return Objects.equals(this.toString(), that.toString())
					&& Objects.equals(this.isAlive, that.isAlive);
		}
		return false;
	}

	public int hashCode() {
		int hashCode = 2 * toString().hashCode() + (isAlive ? 1 : 0);
		return hashCode;
	}

}
