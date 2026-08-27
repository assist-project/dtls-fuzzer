package se.uu.it.dtlsfuzzer.components.sul.core;

import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapperStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import se.uu.it.dtlsfuzzer.components.sul.mapper.MockOutputMapperRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsInputTransformer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilderRA;

public class TlsSULBuilderRA
    implements
        SULBuilder<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> {

    private TlsInputTransformer inputTransformer;

    public TlsSULBuilderRA(TlsInputTransformer inputTransformer) {
        this.inputTransformer = inputTransformer;
    }

    @Override
    public AbstractSUL<
        PSymbolInstance,
        PSymbolInstance,
        TlsExecutionContextRA
    > buildSUL(SULConfig sulConfig, CleanupTasks cleanupTasks) {

        TlsOutputBuilderRA outputBuilder = new TlsOutputBuilderRA();
        MockOutputMapperRA outputMapper = new MockOutputMapperRA(sulConfig.getMapperConfig(), outputBuilder);
        MapperComposerRA<PSymbolInstance, TlsProtocolMessage, TlsExecutionContextRA, TlsState> mapperComposer = new MapperComposerRA<>(null, outputMapper);

        TlsSUL sul = new TlsSULBuilder().buildSUL(sulConfig, cleanupTasks);
        return new TlsSULRA(sul, inputTransformer, mapperComposer);
    }

    @Override
    public SULWrapper<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> buildWrapper() {
        return new SULWrapperStandard<>();
    }
}
