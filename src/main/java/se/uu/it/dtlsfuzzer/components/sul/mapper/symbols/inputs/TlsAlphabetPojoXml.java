package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetPojoXml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * POJO class used for XML (de-)serialization.
 */
@XmlRootElement(name = "alphabet")
@XmlAccessorType(XmlAccessType.FIELD)
public class TlsAlphabetPojoXml extends AlphabetPojoXml<TlsInput> {
    @XmlElements(value = {
            @XmlElement(type = GenericTlsInput.class, name = "GenericTlsInput"),
            @XmlElement(type = ChangeCipherSpecInput.class, name = "ChangeCipherSpecInput"),
            @XmlElement(type = AlertInput.class, name = "AlertInput"),
            @XmlElement(type = ClientHelloInput.class, name = "ClientHelloInput"),
            @XmlElement(type = ClientHelloWithSessionIdInput.class, name = "ClientHelloWithSessionIdInput"),
            @XmlElement(type = ClientHelloRenegotiationInput.class, name = "ClientHelloRenegotiation"),
            @XmlElement(type = HelloVerifyRequestInput.class, name = "HelloVerifyRequestInput"),
            @XmlElement(type = ServerHelloInput.class, name = "ServerHelloInput"),
            @XmlElement(type = ServerHelloDoneInput.class, name = "ServerHelloDoneInput"),
            @XmlElement(type = ServerKeyExchangeInput.class, name = "ServerKeyExchangeInput"),
            @XmlElement(type = CertificateRequestInput.class, name = "CertificateRequestInput"),
            @XmlElement(type = FinishedInput.class, name = "FinishedInput"),
            @XmlElement(type = ClientKeyExchangeInput.class, name = "ClientKeyExchangeInput"),
            @XmlElement(type = CertificateInput.class, name = "CertificateInput"),
            @XmlElement(type = ApplicationInput.class, name = "ApplicationInput"),
            @XmlElement(type = HelloRequestInput.class, name = "HelloRequestInput"),
            @XmlElement(type = EncryptedExtensionsInput.class, name = "EncryptedExtensionsInput"),
            @XmlElement(type = AcknowledgementInput.class, name = "AcknowledgementInput")})
    private List<TlsInput> inputs;

    public TlsAlphabetPojoXml() {
    }

    public TlsAlphabetPojoXml(List<TlsInput> inputs) {
        this.inputs = inputs;
    }

    @Override
    public List<TlsInput> getInputs() {
        return inputs;
    }
}
