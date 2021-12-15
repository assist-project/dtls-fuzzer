package se.uu.it.dtlsfuzzer.config;

public interface RoleProvider {
	/**
	 * @return true if analysis concerns a client implementation, false otherwise
	 */
	boolean isClient();
}
