package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.xml.AbstractInputXml;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TlsInput extends AbstractInputXml {

    public TlsInput() {
        super();
    }

    public TlsInput(String name) {
        super(name);
    }

    @Override
    public void preSendUpdate(ExecutionContext context) {
    }

    @Override
    public abstract TlsProtocolMessage generateProtocolMessage(ExecutionContext context);

    @Override
    public void postSendUpdate(ExecutionContext context) {
    }

    @Override
    public void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
            ExecutionContext context) {
    }

    protected final TlsExecutionContext getTlsExecutionContext(ExecutionContext context) {
        return (TlsExecutionContext) context;
    }

    protected final TlsContext getTlsContext(ExecutionContext context) {
        return ((TlsExecutionContext) context).getState().getTlsContext();
    }

    protected final State getState(ExecutionContext context) {
        return getTlsExecutionContext(context).getState().getState();
    }

    protected final Config getConfig(ExecutionContext context) {
        return getTlsExecutionContext(context).getState().getState().getConfig();
    }

    @Override
    public abstract TlsInputType getInputType();
}
