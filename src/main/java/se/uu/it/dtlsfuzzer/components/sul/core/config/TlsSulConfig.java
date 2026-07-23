package se.uu.it.dtlsfuzzer.components.sul.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;

public interface TlsSulConfig extends SULConfig {
    ConfigDelegate getConfigDelegate();
}
