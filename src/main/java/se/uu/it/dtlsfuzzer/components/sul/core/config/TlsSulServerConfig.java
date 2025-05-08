package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulAdapterConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfigStandard;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class TlsSulServerConfig extends SulServerConfigStandard implements TlsSulConfig {

    @ParametersDelegate
    private ServerConfigDelegate configDelegate = new ServerConfigDelegate();

    public TlsSulServerConfig() {
        super(new MapperConfigStandard(), new SulAdapterConfigStandard());
    }

    @Override
    public ConfigDelegate getConfigDelegate() {
        return configDelegate;
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
        }
    }

    @Override
    public SulConfig cloneWithThreadId(int threadId) {
        TlsSulServerConfig clone = new TlsSulServerConfig();
        clone.setResponseWait(getResponseWait());
        clone.setStartWait(getStartWait());
        clone.processDir = getProcessDir();
        // host
        String originalHost = this.getHost();
        String[] hostParts = originalHost.split(":");
        String hostname = hostParts[0];
        int originalPort = Integer.parseInt(hostParts[1]);
        int newPort = originalPort + threadId;
        String newHost = hostname + ":" + newPort;
        clone.setHost(newHost);
        // command
        String newCommand = this.getCommand().replace("" + originalPort, "" + newPort);
        clone.command = newCommand;

        clone.processTrigger = getProcessTrigger();
        clone.mapperConfig = getMapperConfig();

        // PionDTLS-2-0-9_Server_psk related
        if(getTerminateCommand() != null) {
            clone.terminateCommand = getTerminateCommand().replace("" + originalPort, "" + newPort);
        }

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
