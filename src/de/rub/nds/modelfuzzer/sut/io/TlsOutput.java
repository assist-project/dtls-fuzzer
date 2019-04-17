package de.rub.nds.modelfuzzer.sut.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.transport.socket.SocketState;

public class TlsOutput {
	private List<ProtocolMessage> messages;
	private List<AbstractRecord> records;

	private SocketState state;
	
	private static final TlsOutput SOCKET_CLOSED = new TlsOutput(new ArrayList<>(), new ArrayList<>(), SocketState.CLOSED);
	
	public static TlsOutput socketClosed() {
		return SOCKET_CLOSED;
	}
	
	public TlsOutput(List<ProtocolMessage> messages, List<AbstractRecord> records, SocketState state) {
		this.messages = messages;
		this.records = records;
		this.state = state;
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

	public List<AbstractRecord> getRecords() {
		return records;
	}

	public SocketState getState() {
		return state;
	}
	
	/*
	 * Currently we only look at basically the type of a message when comparing 
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			TlsOutput that = (TlsOutput) obj;
			if (Objects.equals(that.getState(), this.getState())) {
				if (that.messages.size() == messages.size()) {
					for (int i=0; i<messages.size(); i++) {
						if (!messages.get(i).toCompactString()
								.equals(that.getMessages().get(i).toCompactString())) {
							return false;
						}
					}
					return true;
				}
			}
		}
		
		return false;
	}
}
