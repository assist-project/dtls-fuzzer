package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;

public enum TlsInputType {
    CCS, HANDSHAKE, ALERT, APPLICATION, UNKNOWN, HEARTBEAT, EMPTY;

    public static TlsInputType fromTlsMessageType(ProtocolMessageType type) {
        return switch (type) {
            case ALERT -> TlsInputType.ALERT;
            case APPLICATION_DATA -> TlsInputType.APPLICATION;
            case CHANGE_CIPHER_SPEC -> TlsInputType.CCS;
            case HANDSHAKE -> TlsInputType.HANDSHAKE;
            case HEARTBEAT -> TlsInputType.HEARTBEAT;
            case UNKNOWN -> TlsInputType.UNKNOWN;
            default -> {
                throw new RuntimeException("Type not supported: " + type);
            }
        };
    }

}
