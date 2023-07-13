package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.ConfigDelegateProvider;
import se.uu.it.dtlsfuzzer.components.sul.mapper.PhasedMapper;

public class TlsSulBuilder implements SulBuilder {
    private TlsSul tlsSul;

    public TlsSulBuilder(MapperConfig mapperConfig) {
    }

    @Override
    public TlsSul build(SulConfig sulConfig,
            com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks cleanupTasks) {
        return new TlsSul(sulConfig, ((ConfigDelegateProvider) sulConfig).getConfigDelegate(), sulConfig.getMapperConfig(), new PhasedMapper(sulConfig.getMapperConfig()), cleanupTasks);
    }

    public TlsSul getTLSSul() {
        return tlsSul;
    }
}
