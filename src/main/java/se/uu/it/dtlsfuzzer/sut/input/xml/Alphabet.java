package se.uu.it.dtlsfuzzer.sut.input.xml;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import se.uu.it.dtlsfuzzer.sut.input.AlertInput;
import se.uu.it.dtlsfuzzer.sut.input.ApplicationInput;
import se.uu.it.dtlsfuzzer.sut.input.CertificateInput;
import se.uu.it.dtlsfuzzer.sut.input.CertificateRequestInput;
import se.uu.it.dtlsfuzzer.sut.input.ChangeCipherSpecInput;
import se.uu.it.dtlsfuzzer.sut.input.ClientHelloInput;
import se.uu.it.dtlsfuzzer.sut.input.ClientHelloRenegotiationInput;
import se.uu.it.dtlsfuzzer.sut.input.ClientHelloWithSessionIdInput;
import se.uu.it.dtlsfuzzer.sut.input.ClientKeyExchangeInput;
import se.uu.it.dtlsfuzzer.sut.input.FinishedInput;
import se.uu.it.dtlsfuzzer.sut.input.GenericTlsInput;
import se.uu.it.dtlsfuzzer.sut.input.HelloRequestInput;
import se.uu.it.dtlsfuzzer.sut.input.HelloVerifyRequestInput;
import se.uu.it.dtlsfuzzer.sut.input.ServerHelloDoneInput;
import se.uu.it.dtlsfuzzer.sut.input.ServerHelloInput;
import se.uu.it.dtlsfuzzer.sut.input.ServerKeyExchangeInput;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;

/**
 * POJO class used for .xml de-serialization.
 */
@XmlRootElement(name = "alphabet")
@XmlAccessorType(XmlAccessType.FIELD)
class Alphabet {
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
