package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.HelloVerifyRequestMessage;
import de.rub.nds.tlsattacker.core.state.State;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class HelloVerifyRequestInput extends DtlsInput {

    /**
     * option for resetting the digests
     */
    @XmlAttribute(name = "resetDigest", required = false)
    private boolean resetDigest = true;

    @XmlAttribute(name = "digestHR", required = false)
    private boolean digestHR = false;

    public HelloVerifyRequestInput() {
        super("HELLO_VERIFY_REQUEST");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        HelloVerifyRequestMessage hvr = new HelloVerifyRequestMessage(getConfig(context));
        return new TlsProtocolMessage(hvr);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

    public void postSendDtlsUpdate(State state, ExecutionContext context) {
        if (resetDigest) {
            state.getTlsContext().getDigest().reset();
        }
    }
}
