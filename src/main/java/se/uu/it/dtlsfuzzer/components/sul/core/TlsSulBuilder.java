package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsInputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsMapperComposer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsOutputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilder;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class TlsSulBuilder implements SulBuilder<TlsInput, TlsOutput, TlsExecutionContext> {

    private TlsSulAdapter sulAdapter = null;

    public TlsSulBuilder() {
    }

    @Override
    public TlsSul build(SulConfig sulConfig,
            com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks cleanupTasks) {

        TlsOutputBuilder outputBuilder = new TlsOutputBuilder();
        TlsOutputChecker outputChecker = new TlsOutputChecker();

        DtlsOutputMapper outputMapper = new DtlsOutputMapper(sulConfig.getMapperConfig(), outputBuilder, outputChecker);
        DtlsInputMapper inputMapper = new DtlsInputMapper(sulConfig.getMapperConfig(), outputChecker);

        DtlsMapperComposer mapperComposer = new DtlsMapperComposer(inputMapper, outputMapper);

        TlsSul tlsSul = new TlsSul((TlsSulConfig) sulConfig, sulConfig.getMapperConfig(), mapperComposer, cleanupTasks);

        if (sulConfig.getSulAdapterConfig().getAdapterPort() != null) {
            if (sulAdapter == null) {
                sulAdapter = new TlsSulAdapter(sulConfig.getSulAdapterConfig(), cleanupTasks,
                        sulConfig.isFuzzingClient());
            }
            tlsSul.setSulAdapter(sulAdapter);
        }
        return tlsSul;
    }
}
