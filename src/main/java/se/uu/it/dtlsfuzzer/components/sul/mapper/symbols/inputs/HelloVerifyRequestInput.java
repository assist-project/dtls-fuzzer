package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.protocol.message.HelloVerifyRequestMessage;
import de.rub.nds.tlsattacker.core.state.State;
import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class HelloVerifyRequestInput extends DtlsInput {

    /**
     * option for resetting the digests
     */
    @XmlAttribute(name = "resetDigest", required = false)
    private boolean resetDigest = true;

    public HelloVerifyRequestInput() {
        super("HELLO_VERIFY_REQUEST");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        HelloVerifyRequestMessage hvr = new HelloVerifyRequestMessage();
        return new TlsProtocolMessage(hvr);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

    public void postSendDtlsUpdate(State state, TlsExecutionContext context) {
        if (resetDigest) {
            state.getTlsContext().getDigest().reset();
        }
    }
}
