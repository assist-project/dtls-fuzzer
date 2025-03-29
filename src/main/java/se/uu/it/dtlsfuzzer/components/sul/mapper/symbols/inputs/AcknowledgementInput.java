package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.AcknowledgementMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class AcknowledgementInput extends DtlsInput {

    public AcknowledgementInput() {
        super();
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        AcknowledgementMessage ackMessage = new AcknowledgementMessage();

        return new TlsProtocolMessage(ackMessage);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.Acknowledgement;
    }

}
