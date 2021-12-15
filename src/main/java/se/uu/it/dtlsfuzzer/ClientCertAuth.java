package se.uu.it.dtlsfuzzer;

public enum ClientCertAuth {
	REQUIRED,
	OPTIONAL,
	DISABLED;
	
	public boolean isRequired() {
		return this.equals(REQUIRED);
	}
}
