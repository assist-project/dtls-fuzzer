package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.data.DataValue;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInputXml;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsParamRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput extends AbstractInputXml<TlsOutput, TlsProtocolMessage, TlsExecutionContext> {
    @XmlElement(name = "param")
    private List<TlsParamRA> params;

    public TlsInput() {
        super();
    }

    public TlsInput(String name) {
        super(name);
    }

    @Override
    public void preSendUpdate(TlsExecutionContext context) {
    }

    @Override
    public abstract TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context);

    @Override
    public void postSendUpdate(TlsExecutionContext context) {
    }

    @Override
    // FIXME: To be able to override properly an OutputChecker<O> is expected,
    // instead of something that implements outputChecker<O> It might be worth
    // updating the mapperInput class to allow the use of own outputcheckers
    // directly.
    public void postReceiveUpdate(TlsOutput output, OutputChecker<TlsOutput> outputChecker,
            TlsExecutionContext context) {
    }

    public abstract TlsInputType getInputType();

    public List<TlsParamRA> getParams() {
        return params;
    }

    /**
     * Returns the parameters supported for RA learning.
     *
     * @return an immutable list of supported parameters
     */
    public abstract List<TlsParamRA> getSupportedParams();

    /**
     * Checks and appliesvalues for the parameters defined by {@link #params}.
     *
     * @param values values used to instantiate protocol messages
     */
    public final void applyValues(DataValue[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length == params.size()) {
            for (int i=0; i<values.length; i++) {
                if (values[i].getDataType() != params.get(i).getDataType()) {
                    List<DataType> expectedTypes = params.stream().map(TlsParamRA::getDataType).collect(Collectors.toList());
                    List<DataType> actualTypes = Stream.of(values).map(DataValue::getDataType).collect(Collectors.toList());
                    throw new IllegalArgumentException(
                            String.format("""
                                    Input %s received arguments with incompatible types.\
                                    Expected types: %s\
                                    Actual types: %s\
                                    """, getName(), expectedTypes, actualTypes));
                }
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    """
                    Input %s received incorrect number of arguments.\
                    Expected number: %s\
                    Actual number: %s\
                    """, getName(), params.size(), values.length));
        }
        doApplyValues(values);
    }

    protected abstract void doApplyValues(DataValue[] values);
}
