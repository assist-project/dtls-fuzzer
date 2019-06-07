package se.uu.it.modeltester.sut.io;

import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;

public enum TlsInputType {
	CCS, HANDSHAKE, ALERT, APPLICATION, UNKNOWN, HEARTBEAT;

	public static TlsInputType fromProtocolMessageType(ProtocolMessageType type) {
		switch (type) {
			case ALERT :
				return TlsInputType.ALERT;
			case APPLICATION_DATA :
				return TlsInputType.APPLICATION;
			case CHANGE_CIPHER_SPEC :
				return TlsInputType.CCS;
			case HANDSHAKE :
				return TlsInputType.HANDSHAKE;
			case HEARTBEAT :
				return TlsInputType.HEARTBEAT;
			case UNKNOWN :
				return TlsInputType.UNKNOWN;
			default :
				throw new RuntimeException("Type not supported: " + type);
		}
	}

}
