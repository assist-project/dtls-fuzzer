package de.rub.nds.modelfuzzer.sut.io;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class ClientHelloInput extends TlsInput{

	@XmlAttribute(name = "suite", required = true)
	private CipherSuite suite;
	
	
	public ClientHelloInput() {
		super(new InputExecutor(), "CLIENT_HELLO");
	}

	public ClientHelloInput(CipherSuite cipherSuite) {
		super(new InputExecutor(), "CLIENT_HELLO_"+cipherSuite.toString());
		this.suite = cipherSuite;
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getConfig().setDefaultSelectedCipherSuite(suite);
        state.getConfig().setDefaultServerSupportedCiphersuites(suite);
        state.getConfig().setDefaultClientSupportedCiphersuites(Arrays.asList(suite));
        if (suite.name().contains("EC")) {
            state.getConfig().setAddECPointFormatExtension(true);
            state.getConfig().setAddEllipticCurveExtension(true);
        } else {
            state.getConfig().setAddECPointFormatExtension(false);
            state.getConfig().setAddEllipticCurveExtension(false);
        } 
        
        ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
        
		return message;
	}

}
