package se.uu.it.dtlsfuzzer.sut.input;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.constants.AlertDescription;
import de.rub.nds.tlsattacker.core.constants.AlertLevel;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

public class AlertInput extends DtlsInput {

    @XmlAttribute(name = "level", required = true)
    private AlertLevel level;

    @XmlAttribute(name = "description", required = true)
    private AlertDescription description;

    public AlertInput() {
        super();
    }

    @Override
    public TlsMessage generateMessage(State state, ExecutionContext context) {
        AlertMessage alert = new AlertMessage(state.getConfig());
        alert.setConfig(new byte [] {level.getValue(), description.getValue()});
        return alert;
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.ALERT;
    }

}
