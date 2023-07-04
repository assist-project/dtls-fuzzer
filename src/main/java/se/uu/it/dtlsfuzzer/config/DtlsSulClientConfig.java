package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConnectionConfig;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ServerDelegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class DtlsSulClientConfig extends SulClientConfig implements ConfigDelegateProvider {

    @ParametersDelegate
    private ClientConfigDelegate configDelegate;

    public DtlsSulClientConfig() {
        configDelegate = new ClientConfigDelegate();
    }

    @Override
    public void applyDelegate(MapperConnectionConfig config) {
    }


    @Override
    public ConfigDelegate getConfigDelegate() {
        return configDelegate;
    }

    private class ClientConfigDelegate extends ConfigDelegate {
//        @ParametersDelegate
        ClientConfigDelegate() {
        }

        @Override
        public void applyDelegate(Config config) throws ConfigurationException {
            ServerDelegate serverDelegate = new ServerDelegate();
            serverDelegate.setPort(getPort());
            serverDelegate.applyDelegate(config);
            config.getDefaultServerConnection().setTimeout(getResponseWait().intValue());
            config.getDefaultServerConnection().setFirstTimeout(getResponseWait().intValue());
        }
    }
}
