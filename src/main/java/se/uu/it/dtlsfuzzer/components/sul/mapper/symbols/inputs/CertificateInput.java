package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.Collections;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class CertificateInput extends DtlsInput {

    @XmlAttribute(name="empty", required=false)
    private boolean empty = false;

    public CertificateInput() {
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        CertificateMessage message = new CertificateMessage();
        if (empty) {
            message.setCertificateEntryList(Collections.emptyList());
        }
        return new TlsProtocolMessage(message);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }
}
