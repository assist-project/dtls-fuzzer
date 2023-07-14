package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class FinishedInput extends DtlsInput {

    @XmlAttribute(name = "resetMSeq", required = true)
    private boolean resetMSeq = false;

    private int lastSequenceNumber;

    public FinishedInput() {
        super("FINISHED");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        // Uncomment line to print digest, TODO remove this when polishing things up
        // System.out.println(ArrayConverter.bytesToHexString(state.getTlsContext().getDigest().getRawBytes()));
        FinishedMessage message = new FinishedMessage();
        lastSequenceNumber = getTlsContext(context).getDtlsWriteHandshakeMessageSequence();
        return new TlsProtocolMessage(message);
    }

    @Override
    public void postSendDtlsUpdate(TlsExecutionContext context) {
        getTlsContext(context).getDigest().reset();
        // we have to make this change for learning to scale
        getTlsContext(context).setDtlsWriteHandshakeMessageSequence(lastSequenceNumber + 1);
    }

    public void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
            ExecutionContext context) {
        if (resetMSeq) {
            if (TlsOutputChecker.hasChangeCipherSpec(output)) {
                getTlsContext(context).setDtlsWriteHandshakeMessageSequence(0);
            }
        }
        super.postReceiveUpdate(output, abstractOutputChecker, context);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }
}
