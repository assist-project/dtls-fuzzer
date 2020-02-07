package se.uu.it.dtlsfuzzer.sut.io;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.protocol.message.DHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.GOSTClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskRsaClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SrpClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.state.State;

/**
 * An input which resets the premaster of the context prior to generating a key
 * exchange message
 */
public class ClientKeyExchangeInput extends NamedTlsInput {

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
	public ProtocolMessage generateMessage(State state) {
		state.getTlsContext().setPreMasterSecret(null);
		if (this.algorithm == null) {
			throw new RuntimeException("Algorithm not set");
		}
		switch (this.algorithm) {
			case RSA :
				return new RSAClientKeyExchangeMessage(state.getConfig());
			case PSK :
				return new PskClientKeyExchangeMessage(state.getConfig());
			case DH :
				return new DHClientKeyExchangeMessage(state.getConfig());
			case ECDH :
				return new ECDHClientKeyExchangeMessage(state.getConfig());
			case PSK_RSA :
				return new PskRsaClientKeyExchangeMessage(state.getConfig());
			case GOST :
				return new GOSTClientKeyExchangeMessage(state.getConfig());
			case SRP :
				return new SrpClientKeyExchangeMessage(state.getConfig());
			default :
				throw new RuntimeException("Algorithm " + algorithm
						+ " not supported");

		}
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
