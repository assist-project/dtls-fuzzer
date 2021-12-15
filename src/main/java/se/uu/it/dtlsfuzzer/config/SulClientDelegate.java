package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.ParametersDelegate;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class SulClientDelegate extends SulDelegate {
	
	@ParametersDelegate
	private ClientDelegate clientDelegate;

	public SulClientDelegate() {
		super();
		clientDelegate = new ClientDelegate();
	}
	
	public void applyDelegate(Config config) throws ConfigurationException {
		clientDelegate.applyDelegate(config);
		config.getDefaultClientConnection().setTimeout(getTimeout());
	}

	
	@Override
	public final String getRole() {
		return "client";
	}
	
	public final boolean isClient() {
		return true;
	}
	
}
