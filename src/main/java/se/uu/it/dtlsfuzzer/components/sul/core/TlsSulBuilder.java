package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;

import se.uu.it.dtlsfuzzer.components.sul.core.config.ConfigDelegateProvider;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsInputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsOutputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class TlsSulBuilder implements SulBuilder {

    private TlsSulAdapter sulAdapter = null;

    public TlsSulBuilder() {
    }

    @Override
    public TlsSul build(SulConfig sulConfig,
            com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks cleanupTasks) {
        DtlsOutputMapper outputMapper = new DtlsOutputMapper(sulConfig.getMapperConfig());
        TlsSul tlsSul = new TlsSul(sulConfig, ((ConfigDelegateProvider) sulConfig).getConfigDelegate(),
                new MapperComposer(new DtlsInputMapper(sulConfig.getMapperConfig(), new TlsOutputChecker()), outputMapper), outputMapper, cleanupTasks);
        if (sulConfig.getSulAdapterConfig().getAdapterPort() != null) {
            if (sulAdapter == null) {
                sulAdapter = new TlsSulAdapter(sulConfig.getSulAdapterConfig(), cleanupTasks, sulConfig.isFuzzingClient());
            }
            tlsSul.setSulAdapter(sulAdapter);
        }
        return tlsSul;
    }
}
