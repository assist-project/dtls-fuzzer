package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetPojoXml;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO class used for .xml de-serialization.
 */
@XmlRootElement(name = "alphabet")
@XmlAccessorType(XmlAccessType.FIELD)
public class Alphabet extends AlphabetPojoXml {
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
            @XmlElement(type = HelloRequestInput.class, name = "HelloRequestInput")})
    private List<TlsInput> inputs;

    public Alphabet() {
    }

    public Alphabet(List<TlsInput> words) {
        this.inputs = words;
    }

    public List<TlsInput> getWords() {
        return inputs;
    }

}
