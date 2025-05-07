package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulAdapter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposerRA;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsInputTransformer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class TlsSulRA
    implements
        AbstractSul<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> {

    private TlsSul wrappedSul;

    private TlsInputTransformer inputTransformer;

    private MapperComposerRA<PSymbolInstance, TlsProtocolMessage, TlsExecutionContextRA, TlsState> mapperComposer;

    public TlsSulRA(TlsSul sul, TlsInputTransformer inputTransformer, MapperComposerRA<PSymbolInstance, TlsProtocolMessage, TlsExecutionContextRA, TlsState> mapperComposer) {
        this.wrappedSul = sul;
        this.inputTransformer = inputTransformer;
        this.mapperComposer = mapperComposer;
    }

    @Override
    public void pre() {
        wrappedSul.pre();
    }

    @Override
    public void post() {
        wrappedSul.post();
    }

    @Override
    public PSymbolInstance step(PSymbolInstance in) {
        // TODO: Currently missing parameter logic, use the epoch setters when adding parameters.
        TlsInput input = inputTransformer.fromTransformedInput(
            in.getBaseSymbol()
        );
        TlsOutput output = wrappedSul.step(input);
        OutputSymbol base = new OutputSymbol(output.getName());
        return new PSymbolInstance(base);
    }

    @Override
    public SulConfig getSulConfig() {
        return wrappedSul.getSulConfig();
    }

    @Override
    public CleanupTasks getCleanupTasks() {
        return wrappedSul.getCleanupTasks();
    }

    @Override
    public void setDynamicPortProvider(
        DynamicPortProvider dynamicPortProvider
    ) {
        wrappedSul.setDynamicPortProvider(dynamicPortProvider);
    }

    @Override
    public DynamicPortProvider getDynamicPortProvider() {
        return wrappedSul.getDynamicPortProvider();
    }

    @Override
    public Mapper<
        PSymbolInstance,
        PSymbolInstance,
        TlsExecutionContextRA
    > getMapper() {
        return mapperComposer;
    }

    @Override
    public SulAdapter getSulAdapter() {
        return wrappedSul.getSulAdapter();
    }
}
