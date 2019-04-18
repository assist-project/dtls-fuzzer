package de.rub.nds.modelfuzzer.sut.io;

import java.util.ArrayList;
import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;

// TODO we should split TlsSpecificationOutput from the regular output. That way we can get the full output, plus the
// specification output.
public class TlsOutput {
	private List<ProtocolMessage> messages;

	private static final TlsOutput SOCKET_CLOSED = new TlsOutput(new ArrayList<>());
	
	public static TlsOutput socketClosed() {
		return SOCKET_CLOSED;
	}
	
	public TlsOutput() {
	}
	
	public TlsOutput(List<ProtocolMessage> messages) {
		this.messages = messages;
	}
	
	public String toString() {
		// CLIENT_HELLO,SERVER_HELLO...
		String messageString = messages
				.stream()
				.map(m -> m.toCompactString())
				.reduce((s1,s2) -> s1+","+s2)
				.orElse("TIMEOUT");
		return messageString;
	}
	
	public List<ProtocolMessage> getMessages() {
		return messages;
	}

	
	/*
	 * Currently we only look at basically the type of a message when comparing 
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			TlsOutput that = (TlsOutput) obj;
//			if (Objects.equals(that.getState(), this.getState())) {
				if (that.messages.size() == messages.size()) {
					for (int i=0; i<messages.size(); i++) {
						if (!messages.get(i).toCompactString()
								.equals(that.getMessages().get(i).toCompactString())) {
							return false;
						}
					}
					return true;
				}
//			}
		}
		return false;
	}
	

	public int hashCode() {
		int hashCode = 0;
		for (ProtocolMessage message : messages) {
			hashCode += message.toCompactString().hashCode() + hashCode*2; 
		}
		return hashCode;
	}
	
}
