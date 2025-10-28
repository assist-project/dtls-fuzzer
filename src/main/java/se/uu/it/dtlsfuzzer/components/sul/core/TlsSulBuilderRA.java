package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapperStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposerRA;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.words.PSymbolInstance;
import se.uu.it.dtlsfuzzer.components.sul.mapper.MockOutputMapperRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsInputTransformer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilderRA;

public class TlsSulBuilderRA
    implements
        SulBuilder<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> {

    private TlsInputTransformer inputTransformer;

    public TlsSulBuilderRA(TlsInputTransformer inputTransformer) {
        this.inputTransformer = inputTransformer;
    }

    @Override
    public AbstractSul<
        PSymbolInstance,
        PSymbolInstance,
        TlsExecutionContextRA
    > buildSul(SulConfig sulConfig, CleanupTasks cleanupTasks) {

        TlsOutputBuilderRA outputBuilder = new TlsOutputBuilderRA();
        MockOutputMapperRA outputMapper = new MockOutputMapperRA(sulConfig.getMapperConfig(), outputBuilder);
        MapperComposerRA<PSymbolInstance, TlsProtocolMessage, TlsExecutionContextRA, TlsState> mapperComposer = new MapperComposerRA<>(null, outputMapper);

        TlsSul sul = new TlsSulBuilder().buildSul(sulConfig, cleanupTasks);
        return new TlsSulRA(sul, inputTransformer, mapperComposer);
    }

    @Override
    public SulWrapper<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> buildWrapper() {
        return new SulWrapperStandard<>();
    }
}
