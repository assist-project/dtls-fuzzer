package se.uu.it.modeltester.sut.io;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.ClientCertificateType;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.execute.BasicInputExecutor;

public class ClientHelloInput extends NamedTlsInput{

	@XmlAttribute(name = "suite", required = true)
	private CipherSuite suite;
	
	
	public ClientHelloInput() {
		super(new BasicInputExecutor(), "CLIENT_HELLO");
	}

	public ClientHelloInput(CipherSuite cipherSuite) {
		super(new BasicInputExecutor(), "CLIENT_HELLO_"+cipherSuite.toString());
		this.suite = cipherSuite;
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getConfig().setDefaultSelectedCipherSuite(suite);
        state.getConfig().setDefaultServerSupportedCiphersuites(suite);
        state.getConfig().setDefaultClientSupportedCiphersuites(Arrays.asList(suite));
        ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
        
		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
