package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ServerDelegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class SulServerDelegate extends SulDelegate {

	@Parameter(names = "-clientWait", required = false, description = "Time before starting the client")
	private Long clientWait = 50L;
	
	@ParametersDelegate
	private ServerDelegate serverDelegate;
	
	
	public SulServerDelegate() {
		super();
		serverDelegate = new ServerDelegate();
	}
	
	public void applyDelegate(Config config) throws ConfigurationException {
		serverDelegate.applyDelegate(config);
		config.getDefaultServerConnection().setTimeout(getResponseWait());
	}

	public Long getClientWait() {
		return clientWait;
	}

	@Override
	public final String getRole() {
		return "server";
	}
	
	public final boolean isClient() {
		return false;
	}
}
