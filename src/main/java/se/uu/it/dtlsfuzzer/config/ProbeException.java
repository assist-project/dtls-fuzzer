package se.uu.it.dtlsfuzzer.config;

public class ProbeException extends Exception {
	private String msg;

	public ProbeException(String msg) {
		this.msg = msg;
		// TODO Auto-generated constructor stub
	}

	public String getMessage() {
		return msg;
	}
}
