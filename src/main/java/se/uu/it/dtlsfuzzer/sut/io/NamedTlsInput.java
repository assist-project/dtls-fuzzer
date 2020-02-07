package se.uu.it.dtlsfuzzer.sut.io;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A TlsInput with a name, which is used as a unique identifier.
 */
public abstract class NamedTlsInput extends TlsInput {
	/**
	 * The name with which the input can be referred
	 */
	@XmlAttribute(name = "name", required = true)
	private String name = null;

	protected NamedTlsInput() {
		super();
	}

	protected NamedTlsInput(String name) {
		super();
		this.name = name;
	}

	public String toString() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public final String getName() {
		return name;
	}

}
