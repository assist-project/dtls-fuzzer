package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.learnlib.ralib.data.DataValue;
import java.util.Arrays;
import java.util.List;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsParamRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

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

    @Override
    public List<TlsParamRA> getSupportedParams() {
        return Arrays.asList(TlsParamRA.EPOCH_O);
    }

    public DataValue [] extractValues(TlsOutput output) {
        return null;
    }
}
