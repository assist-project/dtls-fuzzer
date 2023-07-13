package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.DHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

/*
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
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        if (algorithm == null) {
            throw new RuntimeException("Algorithm not set");
        }
        TlsMessage ske = null;
        switch (algorithm) {
            case DH :
                ske = new DHEServerKeyExchangeMessage(getConfig(context));
                break;
            case ECDH :
                ske = new ECDHEServerKeyExchangeMessage(getConfig(context));
                break;
            case PSK:
                ske = new PskServerKeyExchangeMessage(getConfig(context));
                break;
            default :
                throw new RuntimeException("Algorithm " + algorithm
                        + " not supported");
        }
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
