package se.uu.it.dtlsfuzzer.sut.input;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;
import se.uu.it.dtlsfuzzer.mapper.Mapper;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput extends AbstractInput {

    public TlsInput() {
        super();
    }

    public TlsInput(String name) {
        super(name);
    }

    public abstract TlsMessage generateMessage(State state, ExecutionContext context);

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

    /**
     * Updates the context after receiving an output.
     */
    public TlsOutput postReceiveUpdate(TlsOutput output, State state,
            ExecutionContext context) {
        return output;
    }

    /**
     * Returns the preferred mapper for this input, or null, if there isn't one, meaning the input does not require alterations to the typical mapping of the input.
     */
    public Mapper getPreferredMapper(SulConfig sulConfig, MapperConfig mapperConfig) {
        return null;
    }

    /**
     * Enables the input for execution.
     */
    public boolean isEnabled(State state, ExecutionContext context) {
        return true;
    }


    @Override
    public void preSendUpdate(
            com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext context) {
    }

    @Override
    public ProtocolMessage generateProtocolMessage(
            com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext context) {
        return null;
    }

    @Override
    public void postSendUpdate(
            com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext context) {

    }

    @Override
    public void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
            com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext context) {
    }

    public abstract TlsInputType getInputType();
}
