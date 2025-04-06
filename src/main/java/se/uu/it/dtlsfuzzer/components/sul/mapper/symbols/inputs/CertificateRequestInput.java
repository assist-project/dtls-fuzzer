package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.modifiablevariable.bytearray.ByteArrayModificationFactory;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.constants.ClientCertificateType;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateRequestMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class CertificateRequestInput extends DtlsInput {

    @XmlAttribute(name = "certificate", required = true)
    ClientCertificateType certificateType;

    public CertificateRequestInput() {
        super("CERTIFICATE_REQUEST");
    }

    public CertificateRequestInput(ClientCertificateType certificateType) {
        super(certificateType.name() + "_CERTIFICATE_REQUEST");
        this.certificateType = certificateType;
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        CertificateRequestMessage message;
        if (context.getConfig().getHighestProtocolVersion().isDTLS13()) {
            message = new CertificateRequestMessage(context.getConfig());
        } else {
            message = new CertificateRequestMessage();
        }
        if (certificateType != null) {
            ModifiableByteArray ctbyte = new ModifiableByteArray();
            ctbyte.setModification(ByteArrayModificationFactory.explicitValue(new byte[] { certificateType.getValue() }));
            message.setClientCertificateTypes(ctbyte);
        }
        return new TlsProtocolMessage(message);
    }

    public ClientCertificateType getClientCertificateType() {
        return certificateType;
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }
}
