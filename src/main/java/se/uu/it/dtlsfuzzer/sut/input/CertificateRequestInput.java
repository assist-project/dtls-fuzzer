package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.modifiablevariable.bytearray.ByteArrayModificationFactory;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.modifiablevariable.integer.ModifiableInteger;
import de.rub.nds.tlsattacker.core.constants.ClientCertificateType;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateRequestMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

public class CertificateRequestInput extends DtlsInput {

	@XmlAttribute(name="certificate", required=true)
	ClientCertificateType certificateType;

	public CertificateRequestInput() {
		super("CERTIFICATE_REQUEST");
	}

	public CertificateRequestInput(ClientCertificateType certificateType) {
		super(certificateType.name() + "_CERTIFICATE_REQUEST");
		this.certificateType = certificateType;
	}

	@Override
	public TlsMessage generateMessage(State state, ExecutionContext context) {
		CertificateRequestMessage message = new CertificateRequestMessage();
		if (certificateType != null) {
			ModifiableByteArray ctbyte = new ModifiableByteArray();
			ctbyte.setModification(ByteArrayModificationFactory.explicitValue(new byte[] {certificateType.getValue()}));
			message.setClientCertificateTypes(ctbyte);
			ModifiableInteger ctcount = new ModifiableInteger();
			ctcount.setOriginalValue(1);
		}
		return message;
	}

	public ClientCertificateType getClientCertificateType() {
		return certificateType;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}
}
