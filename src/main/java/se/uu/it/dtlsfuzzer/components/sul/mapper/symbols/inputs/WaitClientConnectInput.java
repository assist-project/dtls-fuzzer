package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.learnlib.ralib.data.DataValue;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsParamRA;

public class WaitClientConnectInput extends TlsInput {

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        throw new NotImplementedException("Cannot generate message");
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.EMPTY;
    }

    @Override
    public List<TlsParamRA> getSupportedParams() {
        return Collections.emptyList();
    }

    @Override
    public void doApplyValues(DataValue[] values) {
    }
}
