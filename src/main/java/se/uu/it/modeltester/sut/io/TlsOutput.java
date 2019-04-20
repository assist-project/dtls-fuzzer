package se.uu.it.modeltester.sut.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;


/**
 * The outputs used in learning comprise message strings obtained by compacting messages.
 */
public class TlsOutput {
	private List<String> messageStrings;

	private static final TlsOutput SOCKET_CLOSED = new TlsOutput(new ArrayList<>());
	
	public static TlsOutput socketClosed() {
		return SOCKET_CLOSED;
	}
	
	public TlsOutput() {
	}
	
	public TlsOutput(String [] messageStrings) {
		this.messageStrings = Arrays.asList(messageStrings);
	}
	
	public TlsOutput(List<ProtocolMessage> messages) {
		messageStrings = 
				messages.stream()
				.map(m -> m.toCompactString())
				.collect(Collectors.toList());
	}
	
	public String toString() {
		// CLIENT_HELLO,SERVER_HELLO...
		String messageString = messageStrings
				.stream()
				.reduce((s1,s2) -> s1+","+s2)
				.orElse("TIMEOUT");
		return messageString;
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			TlsOutput that = (TlsOutput) obj;
			return Objects.equals(this.messageStrings, that.messageStrings);
		}
		return false;
	}
	

	public int hashCode() {
		int hashCode = 0;
		for (String message : messageStrings) {
			hashCode += message.hashCode() + hashCode*2; 
		}
		return hashCode;
	}
	
}
