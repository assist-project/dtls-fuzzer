package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConnectionConfig;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class DtlsSulServerConfig  extends SulServerConfigStandard implements ConfigDelegateProvider {

    @ParametersDelegate
    private ServerConfigDelegate configDelegate;

    public DtlsSulServerConfig() {
        configDelegate = new ServerConfigDelegate();
    }

    @Override
    public ConfigDelegate getConfigDelegate() {
        return configDelegate;
    }

    @Override
    public void applyDelegate(MapperConnectionConfig config) {
    }


    private class ServerConfigDelegate extends ConfigDelegate {
        ServerConfigDelegate() {
        }

        @Override
        public void applyDelegate(Config config) throws ConfigurationException {
            ClientDelegate clientDelegate = new ClientDelegate();
            clientDelegate.setHost(getHost());
            clientDelegate.applyDelegate(config);
            config.getDefaultServerConnection().setTimeout(getResponseWait().intValue());
            config.getDefaultServerConnection().setFirstTimeout(getResponseWait().intValue());
        }
    }
}
