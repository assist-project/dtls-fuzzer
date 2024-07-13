package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;

/**
 * The ProtocolMessage is a wrapper around a TLS-Attacker ProtocolMessage.
 */
public class TlsProtocolMessage {

    private final ProtocolMessage<? extends ProtocolMessage<?>> message;

    public TlsProtocolMessage(ProtocolMessage<? extends ProtocolMessage<?>> message) {
        this.message = message;
    }

    public ProtocolMessage<? extends ProtocolMessage<?>> getMessage() {
        return message;
    }
}
