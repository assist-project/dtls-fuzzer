package se.uu.it.jsse.examples.dtlsclientserver;

import com.beust.jcommander.Parameter;
//import JCommander.Parameter;

public class DtlsClientServerConfig {
	
	/*
	 * The following is to set up the keystores.
	 */
	private static final String DEFAULT_TRUST_STORE_PASSWORD = "student";
	private static final String DEFAULT_TRUST_STORE_LOCATION = "rsa2048.jks";
	private static final int DEFAULT_PORT = 20000; 
	
	@Parameter(names = "-trustLocation", required = false, description = "The location of the trust store to use")
	private String trustLocation = DEFAULT_TRUST_STORE_LOCATION;
	
	@Parameter(names = "-trustPassword", required = false, description = "The password with which the trust store is protected")
	private String trustPassword = DEFAULT_TRUST_STORE_PASSWORD;
	
	@Parameter(names = "-keyLocation", required = false, description = "The location of the key store to use")
	private String keyLocation = DEFAULT_TRUST_STORE_LOCATION;
	
	@Parameter(names = "-keyPassword", required = false, description = "The location with which the key store is protected")
	private String keyPassword = DEFAULT_TRUST_STORE_PASSWORD;
	
	@Parameter(names = "-threadStarterIpPort", required = false)
	private String threadStarterPortIpPort = null;
	
	@Parameter(names = "-runWait", required = false)
	private Integer runWait = 0;
	
	@Parameter(names = "-hostname", required = false)
	private String hostname = "localhost";
	
	@Parameter(names = "-port", required = false)
	private int port = DEFAULT_PORT;
	
	@Parameter(names = "-client", required = false)
	private boolean client = false;
	
	@Parameter(names = "-auth", required = false)
	private ClientAuth auth = ClientAuth.DISABLED;
	
	@Parameter(names = "-enableResumption", required = false)
	private boolean enableResumption = false;
	
	@Parameter(names = "-operation", required = true)
	private Operation operation;
	
	@Parameter(names = "-enableRetransmission", required = false)
	private boolean enableRetransmission = false;
	
	// some default options
	public DtlsClientServerConfig() {
	}

	public String getThreadStarterIpPort() {
		return threadStarterPortIpPort;
	}
	
	public Integer getRunWait() {
		return runWait;
	}
	
	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}
	
	public boolean isClient() {
		return client;
	}

	public ClientAuth getAuth() {
		return auth;
	}

	public boolean isResumptionEnabled() {
		return enableResumption;
	}

	public boolean isEnableRetransmission() {
		return enableRetransmission;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public void setAuth(ClientAuth auth) {
		this.auth = auth;
	}

	public void setEnableResumption(boolean enableResumption) {
		this.enableResumption = enableResumption;
	}

	public void setEnableRetransmission(boolean enableRetransmission) {
		this.enableRetransmission = enableRetransmission;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	
	public String getTrustLocation() {
		return trustLocation;
	}

	public String getTrustPassword() {
		return trustPassword;
	}

	public String getKeyLocation() {
		return keyLocation;
	}

	public String getKeyPassword() {
		return keyPassword;
	}

}
