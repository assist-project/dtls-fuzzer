package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class RAOutputSymbol extends DtlsInput {

    @Override
    public TlsProtocolMessage generateProtocolMessage(
        TlsExecutionContext context
    ) {
        throw new UnsupportedOperationException(
            "This is not a real protocol message and should therefore never be sent to the SUL, Did you use an RA alphabet for a mealy learning configuration?"
        );
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.UNKNOWN;
    }
}
