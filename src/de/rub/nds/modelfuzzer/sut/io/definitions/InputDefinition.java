package de.rub.nds.modelfuzzer.sut.io.definitions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import de.rub.nds.modelfuzzer.sut.io.ChangeCipherSpecInput;
import de.rub.nds.modelfuzzer.sut.io.ClientHelloInput;
import de.rub.nds.modelfuzzer.sut.io.FinishedInput;
import de.rub.nds.modelfuzzer.sut.io.GenericTlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsInput;

@XmlAccessorType(XmlAccessType.FIELD)
public class InputDefinition {
	private String name;
	@XmlElements(value = {
			@XmlElement(type = TlsInput.class, name = "TlsInput"),
			@XmlElement(type = ClientHelloInput.class, name = "ClientHelloInput"),
			@XmlElement(type = GenericTlsInput.class, name = "GenericTlsInput"),
			@XmlElement(type = FinishedInput.class, name = "FinishedInput"),
			@XmlElement(type = ChangeCipherSpecInput.class, name = "ChangeCipherSpecInput"),
	})
	private TlsInput input;
	
	public InputDefinition(){
	}
	
	public InputDefinition(String name, TlsInput input) {
		super();
		this.name = name;
		this.input = input;
	}

	public String getName() {
		return name;
	}
	public TlsInput getInput() {
		return input;
	}

}
