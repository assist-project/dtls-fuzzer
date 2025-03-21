package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;

/**
 * The ProtocolMessage is a wrapper around a TLS-Attacker ProtocolMessage.
 */
public class TlsProtocolMessage {

    private final ProtocolMessage message;

    public TlsProtocolMessage(ProtocolMessage message) {
        this.message = message;
    }

    public ProtocolMessage getMessage() {
        return message;
    }
}
