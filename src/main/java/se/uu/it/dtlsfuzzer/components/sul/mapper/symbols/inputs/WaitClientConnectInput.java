package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import org.apache.commons.lang3.NotImplementedException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class WaitClientConnectInput extends TlsInput {

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        throw new NotImplementedException("Cannot generate message");
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.EMPTY;
    }

}
