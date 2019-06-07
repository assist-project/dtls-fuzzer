package se.uu.it.modeltester.sut.io;

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
public class AlphabetPojo {
	@XmlElements(value = {
			@XmlElement(type = GenericTlsInput.class, name = "GenericTlsInput"),
			@XmlElement(type = ChangeCipherSpecInput.class, name = "ChangeCipherSpecInput"),
			@XmlElement(type = ClientHelloInput.class, name = "ClientHelloInput"),
			@XmlElement(type = FinishedInput.class, name = "FinishedInput"),})
	private List<TlsInput> inputs;

	public AlphabetPojo() {
	}

	public AlphabetPojo(List<TlsInput> words) {
		this.inputs = words;
	}

	public List<TlsInput> getWords() {
		return inputs;
	}

}
