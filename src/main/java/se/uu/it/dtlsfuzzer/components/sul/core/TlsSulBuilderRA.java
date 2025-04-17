package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.words.PSymbolInstance;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsInputTransformer;

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
    > build(SulConfig sulConfig, CleanupTasks cleanupTasks) {
        TlsSul sul = new TlsSulBuilder().build(sulConfig, cleanupTasks);
        return new TlsSulRA(sul, inputTransformer);
    }
}
