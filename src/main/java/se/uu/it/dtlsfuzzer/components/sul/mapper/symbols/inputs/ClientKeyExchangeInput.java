package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.DHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.GOSTClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskRsaClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SrpClientKeyExchangeMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
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
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        getTlsContext(context).setPreMasterSecret(null);
        ProtocolMessage message = null;
        if (algorithm == null) {
            throw new RuntimeException("Algorithm not set");
        }
        switch (algorithm) {
            case RSA :
                message = new RSAClientKeyExchangeMessage();
                break;
            case PSK :
                message = new PskClientKeyExchangeMessage();
                break;
            case DH :
                message = new DHClientKeyExchangeMessage();
                break;
            case ECDH :
                message = new ECDHClientKeyExchangeMessage();
                break;
            case PSK_RSA :
                message = new PskRsaClientKeyExchangeMessage();
                break;
            case GOST :
                message = new GOSTClientKeyExchangeMessage();
                break;
            case SRP :
                message = new SrpClientKeyExchangeMessage();
                break;
            default :
                throw new RuntimeException("Algorithm " + algorithm
                        + " not supported");

        }

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
