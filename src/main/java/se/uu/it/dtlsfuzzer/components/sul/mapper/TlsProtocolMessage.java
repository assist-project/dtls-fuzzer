package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;

/**
 * The ProtocolMessage is a wrapper around a TLS-Attacker ProtocolMessage.
 */
public class TlsProtocolMessage implements ProtocolMessage {

    private final TlsMessage message;

    public TlsProtocolMessage(TlsMessage message) {
        this.message = message;
    }

    public TlsMessage getMessage() {
        return message;
    }
}
