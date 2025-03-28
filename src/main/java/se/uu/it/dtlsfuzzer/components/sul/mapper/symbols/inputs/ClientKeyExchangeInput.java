package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.DHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.GOSTClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskRsaClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SrpClientKeyExchangeMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

/**
 * An input which resets the pre-master of the context prior to generating a key
 * exchange message
 */
public class ClientKeyExchangeInput extends DtlsInput {

    @XmlAttribute(name = "algorithm", required = true)
    private KeyExchangeAlgorithm algorithm;

    public ClientKeyExchangeInput() {
        super("CLIENT_KEY_EXCHANGE");
    }

    public ClientKeyExchangeInput(KeyExchangeAlgorithm algorithm) {
        super(algorithm + "CLIENT_KEY_EXCHANGE");
        this.algorithm = algorithm;
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        context.getTlsContext().setPreMasterSecret(null);
        if (algorithm == null) {
            throw new RuntimeException("Algorithm not set");
        }
        ProtocolMessage message = switch (algorithm) {
            case RSA -> new RSAClientKeyExchangeMessage();
            case PSK -> new PskClientKeyExchangeMessage();
            case DH -> new DHClientKeyExchangeMessage();
            case ECDH -> new ECDHClientKeyExchangeMessage();
            case PSK_RSA -> new PskRsaClientKeyExchangeMessage();
            case GOST -> new GOSTClientKeyExchangeMessage();
            case SRP -> new SrpClientKeyExchangeMessage();
            default ->
                throw new RuntimeException("Algorithm " + algorithm + " not supported");
        };
        return new TlsProtocolMessage(message);
    }

    public KeyExchangeAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
