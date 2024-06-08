package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulAdapterConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConnectionConfig;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class DtlsSulServerConfig  extends SulServerConfigStandard implements ConfigDelegateProvider {

    @ParametersDelegate
    private ServerConfigDelegate configDelegate = new ServerConfigDelegate();

    public DtlsSulServerConfig() {
        super(new MapperConfigStandard(), new SulAdapterConfigStandard());
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
            super.applyDelegate(config);
            ClientDelegate clientDelegate = new ClientDelegate();
            clientDelegate.setHost(getHost());
            clientDelegate.applyDelegate(config);
            config.getDefaultClientConnection().setTimeout(getResponseWait().intValue());
            config.getDefaultClientConnection().setFirstTimeout(getResponseWait().intValue());
        }
    }
}
