package se.uu.it.jsse.examples.dtlsclientserver;

public enum Operation {
	/**
	 * Basic mode of operation entails performing a single handshake.
	 * No data is exchanged.
	 */
	BASIC,
	
	/**
	 * Basic operation + one echo if the handshake is completed successfully.
	 */
	ONE_ECHO,
	
	/**
	 * Full mode of operation entails a continuous loop of handshaking and echo-ing data.
	 * In this mode the server only terminates if the engine is closed. 
	 */
	FULL,
	
	/**
	 * Full mode of operation in a continuous loop of handshking and echo-ing of data.
	 * In this mode, the server rebuilds the engine when closed, allowing for session resumption to take place.  
	 */
	FULL_SR;
}
