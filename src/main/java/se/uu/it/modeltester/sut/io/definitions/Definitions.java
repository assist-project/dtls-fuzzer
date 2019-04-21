package se.uu.it.modeltester.sut.io.definitions;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import se.uu.it.modeltester.sut.io.TlsInput;

@XmlRootElement(name = "Definitions")
@XmlAccessorType(XmlAccessType.FIELD)
public class Definitions {
	
	@XmlElements(value = {
			@XmlElement(type = InputDefinition.class, name = "InputDefinition")
	})
	private List<InputDefinition> inputDefinitions;
	
	public Definitions() {
		inputDefinitions = new LinkedList<>();
	}
	
	public boolean addInputDefinition(String name, TlsInput input) {
		 TlsInput existingInput = getInputWithDefinition(name);
		 if (existingInput == null) {
			 inputDefinitions.add(new InputDefinition(name, input));
			 return true;
		 }
		 return false;
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
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Definitions: \n");
		for (InputDefinition def : inputDefinitions) {
			builder.append("   ").append(def.getName()).append(":").append(def.getInput()).append("\n");
		}
		return builder.toString();
	}
}
