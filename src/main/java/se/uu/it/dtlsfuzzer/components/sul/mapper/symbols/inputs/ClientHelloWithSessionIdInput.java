package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.ResetConnectionAction;
import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class ClientHelloWithSessionIdInput extends DtlsInput {

    public ClientHelloWithSessionIdInput() {
        super("CLIENT_HELLO_SR");
    }

    protected ClientHelloWithSessionIdInput(String name) {
        super(name);
    }

    @XmlAttribute(name = "suite", required = false)
    private CipherSuite suite;

    /**
     * Resetting the cookie may prompt a long resumption, which is resumption
     * including a server HelloVerifyRequest
     */
    @XmlAttribute(name = "resetCookie", required = false)
    private boolean resetCookie = false;

    private void resetTransportHandler(State state) {
        ResetConnectionAction resetAction = new ResetConnectionAction();
        resetAction.setConnectionAlias(state.getTlsContext().getConnection()
                .getAlias());
        resetAction.execute(state);

        if (resetCookie) {
            state.getTlsContext().setDtlsCookie(null);
        }
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        // reset and resume the connection
        resetTransportHandler(getState(context));
        if (suite != null) {
            getConfig(context).setDefaultClientSupportedCipherSuites(suite);
        }
        ClientHelloMessage message = new ClientHelloMessage(getConfig(context));
        message.setSessionId(getTlsContext(context).getChooser()
                .getServerSessionId());

        return new TlsProtocolMessage(message);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
