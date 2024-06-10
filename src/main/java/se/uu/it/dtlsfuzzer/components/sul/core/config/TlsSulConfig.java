package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;

public interface TlsSulConfig extends SulConfig {
    ConfigDelegate getConfigDelegate();
}
