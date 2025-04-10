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
        lastSequenceNumber = context.getTlsContext().getWriteSequenceNumber(context.getTlsContext().getWriteEpoch());
        // context.getTlsContext().setWriteEpoch(context.getTlsContext().getWriteEpoch() + 1);
        // context.getTlsContext().setWriteSequenceNumber(context.getTlsContext().getWriteEpoch(), 0L);
        return new TlsProtocolMessage(message);
    }

    @Override
    public void postSendDtlsUpdate(TlsExecutionContext context) {
        if (context.getTlsContext().getConfig().getHighestProtocolVersion().isDTLS13() && !context.getTlsContext().isDtls13ShouldSendFinished()){
            // invalid Finished message, we shouldn't care
            return;
        }
        context.getTlsContext().getDigest().reset();

        // in DTLS 1.3, we need to skip the bottom code, otherwise we will start sending message with Epoch=3 and Seq=2
        // However, we prefer to send message with fresh sequence number, which is Epoch=3 and Seq=0
        // Without skip we can also complete handshake, but I prefer to make Seq number more correct.
        // TLS-Attacker will maintain the sequence number for us, so we don't need to manually set sequence number
        if (context.getTlsContext().getConfig().getHighestProtocolVersion().isDTLS13()){
            return;
        }
        // we have to make this change for learning to scale
        context.getTlsContext().setWriteSequenceNumber(context.getTlsContext().getWriteEpoch(), lastSequenceNumber + 1);
    }

    @Override
    public void postReceiveUpdate(TlsOutput output, OutputChecker<TlsOutput> abstractOutputChecker,
            TlsExecutionContext context) {
        if (resetMSeq) {
            if (TlsOutputChecker.hasChangeCipherSpec(output)) {
                context.getTlsContext().setWriteSequenceNumber(context.getTlsContext().getWriteEpoch(), 0);
            }
        }
        super.postReceiveUpdate(output, abstractOutputChecker, context);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }
}
