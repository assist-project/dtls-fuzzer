package de.rub.nds.modelfuzzer.sut.io.definitions;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;

@XmlRootElement(name = "Definitions")
@XmlAccessorType(XmlAccessType.FIELD)
public class Definitions {
	
	public static Definitions generateInputDefinitions(List<TlsInput> inputs) {
		List<InputDefinition> definitions = new LinkedList<>();
		for (TlsInput input : inputs) {
			definitions.add(new InputDefinition(input.toString(), input));
		}
		return new Definitions(definitions);
	}
	
	@XmlElements(value = {
			@XmlElement(type = InputDefinition.class, name = "InputDefinition")
	})
	private List<InputDefinition> inputDefinitions;
	
	
	
	public Definitions() {
		inputDefinitions = new LinkedList<>();
	}
	
	public Definitions(List<InputDefinition> inputDefinitions) {
		this.inputDefinitions = inputDefinitions;
	}
	
	
	public List<InputDefinition>  getInputDefinitions() {
		return inputDefinitions;
	}
	
	public TlsInput getInputWithDefinition(String name) {
		for (InputDefinition def : inputDefinitions) {
			if (def.getName().equals(name))
				return def.getInput();
		}
		return null;
	}
}
