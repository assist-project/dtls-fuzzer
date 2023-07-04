package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import java.util.Collections;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

public class CertificateInput extends DtlsInput {

    @XmlAttribute(name="empty", required=false)
    private boolean empty = false;

    public CertificateInput() {
    }

    @Override
    public TlsMessage generateMessage(State state, ExecutionContext context) {
        CertificateMessage message = new CertificateMessage();
        if (empty) {
            message.setCertificateListConfig(Collections.emptyList());
        }
        return message;
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }
}
