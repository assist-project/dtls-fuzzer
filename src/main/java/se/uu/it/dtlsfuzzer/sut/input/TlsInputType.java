package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;

public enum TlsInputType {
    CCS, HANDSHAKE, ALERT, APPLICATION, UNKNOWN, HEARTBEAT, EMPTY;

    public static TlsInputType fromTlsMessageType(ProtocolMessageType type) {
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
