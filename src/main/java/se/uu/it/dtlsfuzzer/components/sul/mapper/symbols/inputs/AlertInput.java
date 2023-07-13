package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.constants.AlertDescription;
import de.rub.nds.tlsattacker.core.constants.AlertLevel;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class AlertInput extends DtlsInput {

    @XmlAttribute(name = "level", required = true)
    private AlertLevel level;

    @XmlAttribute(name = "description", required = true)
    private AlertDescription description;

    public AlertInput() {
        super();
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        AlertMessage alert = new AlertMessage(getConfig(context));
        alert.setConfig(new byte [] {level.getValue(), description.getValue()});
        return new TlsProtocolMessage(alert);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.ALERT;
    }
}
