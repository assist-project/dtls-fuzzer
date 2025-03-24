package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.DHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskServerKeyExchangeMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

/**
 * This input assumes that DH is DHE and ECDH is ECDHE
 */
public class ServerKeyExchangeInput extends DtlsInput {

    @XmlAttribute(name = "algorithm", required = true)
    private KeyExchangeAlgorithm algorithm;

    public ServerKeyExchangeInput() {
        super("SERVER_KEY_EXCHANGE");
    }

    public ServerKeyExchangeInput(KeyExchangeAlgorithm algorithm) {
        super(algorithm + "_SERVER_KEY_EXCHANGE");
        this.algorithm = algorithm;
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        if (algorithm == null) {
            throw new RuntimeException("Algorithm not set");
        }
        ProtocolMessage ske = switch (algorithm) {
            case DH -> new DHEServerKeyExchangeMessage();
            case ECDH -> new ECDHEServerKeyExchangeMessage();
            case PSK -> new PskServerKeyExchangeMessage();
            default ->
                throw new RuntimeException("Algorithm " + algorithm + " not supported");
        };
        return new TlsProtocolMessage(ske);
    }

    public KeyExchangeAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
