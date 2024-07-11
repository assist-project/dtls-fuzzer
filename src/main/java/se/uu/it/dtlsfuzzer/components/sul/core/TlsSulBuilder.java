package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsInputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsOutputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class TlsSulBuilder implements SulBuilder<TlsInput, TlsOutput, TlsExecutionContext> {

    private TlsSulAdapter sulAdapter = null;

    public TlsSulBuilder() {
    }

    @Override
    public TlsSul build(SulConfig sulConfig,
            com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks cleanupTasks) {
        DtlsOutputMapper outputMapper = new DtlsOutputMapper(sulConfig.getMapperConfig(), new TlsOutputBuilder());
        TlsSul tlsSul = new TlsSul((TlsSulConfig) sulConfig, sulConfig.getMapperConfig(),
                new MapperComposer<TlsInput, TlsOutput, TlsProtocolMessage, TlsExecutionContext, TlsState>(
                        new DtlsInputMapper(sulConfig.getMapperConfig(), new TlsOutputChecker()), outputMapper),
                cleanupTasks);
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
