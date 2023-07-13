package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.HelloRequestMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.Arrays;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class HelloRequestInput extends DtlsInput {

    @XmlAttribute(name = "resetSequenceNumber", required = false)
    private boolean resetSequenceNumber = true;

    @XmlAttribute(name = "disableOnRefusal", required = false)
    private boolean disableOnRefusal = true;

    @XmlAttribute(name = "retransmittedCHAsRefusal")
    private boolean retransmittedCHAsRefusal = true;

    private Integer origMsgSeqNum;
    private byte[] clientRandom;

    public HelloRequestInput() {
        super("HELLO_REQUEST");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        if (resetSequenceNumber) {
            origMsgSeqNum = getTlsContext(context).getDtlsWriteHandshakeMessageSequence();
            getTlsContext(context).setDtlsWriteHandshakeMessageSequence(0);
        }
        if (retransmittedCHAsRefusal) {
            clientRandom = getTlsContext(context).getClientRandom();
        }
        HelloRequestMessage hvr = new HelloRequestMessage(getConfig(context));
        return new TlsProtocolMessage(hvr);
    }

    @Override
    public void postSendDtlsUpdate(TlsExecutionContext context) {
        context.updateRenegotiationIndex();
    }

    @Override
    public void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
            ExecutionContext context) {
        if (!(TlsOutputChecker.hasClientHello((TlsOutput) output))) {
            if (disableOnRefusal) {
                context.disableExecution();
            } else if (resetSequenceNumber) {
                getTlsContext(context).setDtlsWriteHandshakeMessageSequence(origMsgSeqNum);
            }
        } else if (disableOnRefusal && retransmittedCHAsRefusal
                && Arrays.equals(clientRandom, getTlsContext(context).getClientRandom())) {
            context.disableExecution();
        }
        super.postReceiveUpdate(output, abstractOutputChecker, context);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
