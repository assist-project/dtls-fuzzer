package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class FinishedInput extends DtlsInput {

    @XmlAttribute(name = "resetMSeq", required = true)
    private boolean resetMSeq = false;

    private long lastSequenceNumber;

    public FinishedInput() {
        super("FINISHED");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        // Uncomment line to print digest, TODO remove this when polishing things up
        // System.out.println(ArrayConverter.bytesToHexString(state.getTlsContext().getDigest().getRawBytes()));
        FinishedMessage message = new FinishedMessage();
        lastSequenceNumber = getTlsContext(context).getWriteSequenceNumber(getTlsContext(context).getWriteEpoch());
//        getTlsContext(context).setWriteEpoch(getTlsContext(context).getWriteEpoch() + 1);
//        getTlsContext(context).setWriteSequenceNumber(getTlsContext(context).getWriteEpoch(), 0L);
        return new TlsProtocolMessage(message);
    }

    @Override
    public void postSendDtlsUpdate(TlsExecutionContext context) {
        getTlsContext(context).getDigest().reset();
        // we have to make this change for learning to scale
        getTlsContext(context).setWriteSequenceNumber(getTlsContext(context).getWriteEpoch(), lastSequenceNumber + 1);
    }

    @Override
    public void postReceiveUpdate(TlsOutput output, OutputChecker<TlsOutput> abstractOutputChecker,
            TlsExecutionContext context) {
        if (resetMSeq) {
            if (TlsOutputChecker.hasChangeCipherSpec(output)) {
                getTlsContext(context).setWriteSequenceNumber(getTlsContext(context).getWriteEpoch(), 0);
            }
        }
        super.postReceiveUpdate(output, abstractOutputChecker, context);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }
}
