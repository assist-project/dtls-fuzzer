package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.protocol.message.AcknowledgementMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class AcknowledgementInput extends DtlsInput {

    public AcknowledgementInput() {
        super();
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        AcknowledgementMessage ackMessage = new AcknowledgementMessage();

        return new TlsProtocolMessage(ackMessage);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.Acknowledgement;
    }

}
