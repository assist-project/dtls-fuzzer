package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInputXml;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.layer.context.TlsContext;
import de.rub.nds.tlsattacker.core.state.State;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput extends AbstractInputXml<TlsOutput, TlsProtocolMessage, TlsExecutionContext> {

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

    // TODO: Consider whether these should still be in the API now that casts are
    // largely unneccesary.
    protected final TlsExecutionContext getTlsExecutionContext(TlsExecutionContext context) {
        return context;
    }

    protected final TlsContext getTlsContext(TlsExecutionContext context) {
        return context.getState().getTlsContext();
    }

    protected final State getState(TlsExecutionContext context) {
        return getTlsExecutionContext(context).getState().getState();
    }

    protected final Config getConfig(TlsExecutionContext context) {
        return getTlsExecutionContext(context).getState().getState().getConfig();
    }

    // TODO: why did this have an override?
    public abstract TlsInputType getInputType();
}
