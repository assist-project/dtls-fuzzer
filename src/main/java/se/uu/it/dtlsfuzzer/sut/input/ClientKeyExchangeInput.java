package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.protocol.message.DHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.GOSTClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskRsaClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SrpClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

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
    public TlsMessage generateMessage(State state, ExecutionContext context) {
        state.getTlsContext().setPreMasterSecret(null);
        TlsMessage message;
        if (algorithm == null) {
            throw new RuntimeException("Algorithm not set");
        }
        switch (algorithm) {
            case RSA :
                message = new RSAClientKeyExchangeMessage(state.getConfig());
                break;
            case PSK :
                message = new PskClientKeyExchangeMessage(state.getConfig());
                break;
            case DH :
                message = new DHClientKeyExchangeMessage(state.getConfig());
                break;
            case ECDH :
                message = new ECDHClientKeyExchangeMessage(state.getConfig());
                break;
            case PSK_RSA :
                message = new PskRsaClientKeyExchangeMessage(state.getConfig());
                break;
            case GOST :
                message = new GOSTClientKeyExchangeMessage(state.getConfig());
                break;
            case SRP :
                message = new SrpClientKeyExchangeMessage(state.getConfig());
                break;
            default :
                throw new RuntimeException("Algorithm " + algorithm
                        + " not supported");

        }

        return message;
    }

    public KeyExchangeAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
