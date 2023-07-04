package se.uu.it.dtlsfuzzer.sut;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import se.uu.it.dtlsfuzzer.config.ConfigDelegateProvider;
import se.uu.it.dtlsfuzzer.mapper.AbstractMapper;
import se.uu.it.dtlsfuzzer.mapper.PhasedMapper;

public class TlsSULBuilder implements SulBuilder {
    private TlsSUL tlsSul;

    public TlsSULBuilder(MapperConfig mapperConfig, AbstractMapper defaultExecutor) {
    }

    @Override
    public AbstractSul build(SulConfig sulConfig,
            com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks cleanupTasks) {
        return new TlsSUL(sulConfig, ((ConfigDelegateProvider) sulConfig).getConfigDelegate(), sulConfig.getMapperConfig(), new PhasedMapper(sulConfig.getMapperConfig()), cleanupTasks);
    }

    public TlsSUL getTLSSul() {
        return tlsSul;
    }
}
