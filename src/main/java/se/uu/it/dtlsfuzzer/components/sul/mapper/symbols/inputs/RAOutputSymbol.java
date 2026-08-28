package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.learnlib.ralib.data.DataValue;

import java.math.BigDecimal;
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

    /**
     * Fetches the values for the parameters defined by {@link #params}.
     *
     * @param values values for parameters (fields) from messages in a TlsOutput
     */
    // TODO This method currently returns mock 0 values. Params need further developed.
    // We also need to record records/fragments in output, if we want to access fields in them.
    public DataValue[] fetchValues(TlsOutput output) {
        DataValue[] values = new DataValue[super.getParams().size()];
        Arrays.setAll(values, i -> new DataValue(super.getParams().get(i).getDataType(), BigDecimal.ZERO));
        return values;
    }
}
