package se.uu.it.dtlsfuzzer.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConnectionConfig;

public class ConfigWrapper implements MapperConnectionConfig {
    private de.rub.nds.tlsattacker.core.config.Config config;

    public ConfigWrapper(de.rub.nds.tlsattacker.core.config.Config config) {
        this.config = config;
    }

    public de.rub.nds.tlsattacker.core.config.Config getConfig() {
        return config;
    }
}
