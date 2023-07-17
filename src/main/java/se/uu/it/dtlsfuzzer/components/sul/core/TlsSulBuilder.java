package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.ConfigDelegateProvider;
import se.uu.it.dtlsfuzzer.components.sul.mapper.PhasedMapper;

public class TlsSulBuilder implements SulBuilder {

    private TlsSulAdapter sulAdapter = null;

    public TlsSulBuilder() {
    }

    @Override
    public TlsSul build(SulConfig sulConfig,
            com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks cleanupTasks) {
        TlsSul tlsSul = new TlsSul(sulConfig, ((ConfigDelegateProvider) sulConfig).getConfigDelegate(), sulConfig.getMapperConfig(), new PhasedMapper(sulConfig.getMapperConfig()), cleanupTasks);
        if (sulConfig.getSulAdapterConfig().getAdapterPort() != null) {
            if (sulAdapter == null) {
                sulAdapter = new TlsSulAdapter(sulConfig.getSulAdapterConfig(), cleanupTasks, sulConfig.isFuzzingClient());
            }
            tlsSul.setSulAdapter(sulAdapter);
        }
        return tlsSul;
    }
}
