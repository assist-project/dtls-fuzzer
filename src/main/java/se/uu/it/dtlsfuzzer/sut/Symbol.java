package se.uu.it.dtlsfuzzer.sut;

import javax.xml.bind.annotation.XmlAttribute;

public class Symbol {
	/**
	 * The name (abstraction) by which the symbol can be referred.
	 * A name uniquely determines a symbol.
	 */
	@XmlAttribute(name = "name", required = true)
	private String name = null;
	
	private boolean input = false;

	protected Symbol(boolean input) {
		this.input = input;
	}
	
	public Symbol(String name, boolean input) {
		this.name = name;
		this.input = input;
	}
	
	protected void setName(String name) {
		this.name = name;
	}
	
	public String name() {
		return name;
	}
	
	public boolean isInput() {
		return input;
	}
	
	public String toString() {
		return name;
	}
	
	public String inputDistinguishingName() {
		return (isInput() ? "I_" : "O_") + name; 
	}
}
