package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;

public interface TlsSulConfig extends SULConfig {
    ConfigDelegate getConfigDelegate();
}
