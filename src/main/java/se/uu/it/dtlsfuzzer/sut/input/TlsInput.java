package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.config.MapperConfig;
import se.uu.it.dtlsfuzzer.config.SulDelegate;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;
import se.uu.it.dtlsfuzzer.mapper.Mapper;
import se.uu.it.dtlsfuzzer.sut.Symbol;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput extends Symbol {

    @XmlAttribute(name = "extendedWait", required = false)
    private Integer extendedWait;

    protected TlsInput() {
        super(true);
    }

    protected TlsInput (String name) {
        super(name, true);
    }

    /**
     * Returns the preferred mapper for this input, or null, if there isn't one, meaning the input does not require alterations to the typical mapping of the input.
     */
    public Mapper getPreferredMapper(SulDelegate sulConfig, MapperConfig mapperConfig) {
        return null;
    }

    public abstract TlsMessage generateMessage(State state, ExecutionContext context);

    /**
     * Enables the input for execution.
     */
    public boolean isEnabled(State state, ExecutionContext context) {
        return true;
    }

    /**
     * Updates context before sending the input
     */
    public void preSendUpdate(State state, ExecutionContext context) {
    }

    /**
     * Updates the context after sending the input.
     */
    public void postSendUpdate(State state, ExecutionContext context) {
    }

    public Integer getExtendedWait() {
        return extendedWait;
    }

    public void setExtendedWait(Integer extendedWait) {
        this.extendedWait = extendedWait;
    }

    /**
     * Updates the context after receiving an output.
     */
    public TlsOutput postReceiveUpdate(TlsOutput output, State state,
            ExecutionContext context) {
        return output;
    }

    /**
     * The type of the input should correspond to the type of the message the
     * input generates.
     */
    public abstract TlsInputType getInputType();
}
