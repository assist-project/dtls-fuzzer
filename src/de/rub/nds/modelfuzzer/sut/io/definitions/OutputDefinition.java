package de.rub.nds.modelfuzzer.sut.io.definitions;

import de.rub.nds.modelfuzzer.sut.io.TlsOutput;

public class OutputDefinition {
	private String name;
	
	private TlsOutput output;
	
	public OutputDefinition(){
	}
	
	public OutputDefinition(String name, TlsOutput output) {
		super();
		this.name = name;
		this.output = output;
	}

	public String getName() {
		return name;
	}
	public TlsOutput getOutput() {
		return output;
	}

}
