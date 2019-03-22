package de.rub.nds.modelfuzzer.sut.io;

import java.nio.charset.Charset;
import java.util.Arrays;

import de.rub.nds.modelfuzzer.sut.InputExecutor;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class ClientHelloInput extends TlsInput{

	private CipherSuite suite;

	public ClientHelloInput(CipherSuite cipherSuite) {
		super(new InputExecutor());
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
        } if (suite.isPsk()) {
        	state.getConfig().setDefaultPSKKey(new byte [] {0x12, 0x34});
        	state.getConfig().setDefaultPSKIdentity("Client_identity".getBytes(Charset.forName("UTF-8")));
        }
        
        // GnuTls adaptation:
        state.getConfig().setAddHeartbeatExtension(true);
        ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
        
		return message;
	}

	@Override
	public String toString() {
		return "CLIENT_HELLO_"+suite.toString();
	}

	@Override
	public void postUpdate(TlsOutput output, State state) {
	}
	
}
