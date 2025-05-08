package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulAdapterConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfigStandard;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ServerDelegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class TlsSulClientConfig extends SulClientConfigStandard implements TlsSulConfig {

    @ParametersDelegate
    private ClientConfigDelegate configDelegate = new ClientConfigDelegate();

    public TlsSulClientConfig() {
        super(new MapperConfigStandard(), new SulAdapterConfigStandard());
    }

    @Override
    public ConfigDelegate getConfigDelegate() {
        return configDelegate;
    }

    private class ClientConfigDelegate extends ConfigDelegate {
        ClientConfigDelegate() {
        }

        @Override
        public void applyDelegate(Config config) throws ConfigurationException {
            super.applyDelegate(config);
            ServerDelegate serverDelegate = new ServerDelegate();
            serverDelegate.setPort(getPort());
            serverDelegate.applyDelegate(config);
            config.getDefaultServerConnection().setTimeout(getResponseWait().intValue());
        }
    }

    @Override
    public SulConfig cloneWithThreadId(int threadId) {
        TlsSulClientConfig clone = new TlsSulClientConfig();
        clone.clientWait = clientWait;
        int oldPort = getPort();
        int newPort = getPort() + threadId;
        clone.setPort(newPort);
        clone.setResponseWait(getResponseWait());
        String newCommand = this.getCommand().replace("" + oldPort, "" + newPort);
        clone.command = newCommand;
        clone.setStartWait(getStartWait());
        clone.processDir = getProcessDir();

        clone.processTrigger = getProcessTrigger();
        clone.mapperConfig = getMapperConfig();

        // Scandium-2-0-0-M16 config related
        if (clone.command.contains("-starterAddress localhost:")) {
            int adapterPort = this.sulAdapterConfig.getAdapterPort();
            int newAdapterPort = adapterPort + threadId;
            clone.command = clone.command.replace("-starterAddress localhost:"+adapterPort, "-starterAddress localhost:"+(newAdapterPort));
            clone.sulAdapterConfig = new SulAdapterConfigStandard(newAdapterPort, this.sulAdapterConfig.getAdapterAddress());
        }

        // JSSE-12-0-2 related
        if (clone.command.contains("-threadStarterIpPort localhost:")) {
            int adapterPort = this.sulAdapterConfig.getAdapterPort();
            int newAdapterPort = adapterPort + threadId;
            clone.command = clone.command.replace("-threadStarterIpPort localhost:"+adapterPort, "-threadStarterIpPort localhost:"+(newAdapterPort));
            clone.sulAdapterConfig = new SulAdapterConfigStandard(newAdapterPort, this.sulAdapterConfig.getAdapterAddress());
        }

        return clone;
    }
}
