package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapperStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsInputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsOutputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilder;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class TlsSulBuilder implements SULBuilder<TlsInput, TlsOutput, TlsExecutionContext> {

    private TlsSulAdapter sulAdapter = null;

    @Override
    public TlsSul buildSUL(SULConfig sulConfig, CleanupTasks cleanupTasks) {
        MapperConfig mapperConfig = sulConfig.getMapperConfig();
        TlsOutputChecker outputChecker = new TlsOutputChecker();
        TlsOutputBuilder outputBuilder = new TlsOutputBuilder();

        DtlsOutputMapper outputMapper = new DtlsOutputMapper(mapperConfig, outputBuilder, outputChecker);
        DtlsInputMapper inputMapper = new DtlsInputMapper(mapperConfig, outputChecker);
        MapperComposer<TlsInput, TlsOutput, TlsProtocolMessage, TlsExecutionContext, TlsState> mapperComposer = new MapperComposer<>(inputMapper, outputMapper);

        TlsSul tlsSul = new TlsSul((TlsSulConfig) sulConfig, mapperConfig, mapperComposer, cleanupTasks);

        if (sulConfig.getSULAdapterConfig().getAdapterPort() != null) {
            sulAdapter = new TlsSulAdapter(sulConfig.getSULAdapterConfig(), cleanupTasks, sulConfig.isFuzzingClient());
            tlsSul.setSulAdapter(sulAdapter);
        }
        return tlsSul;
    }


    @Override
    public SULWrapper<TlsInput, TlsOutput, TlsExecutionContext> buildWrapper() {
        return new SULWrapperStandard<>();
    }
}
