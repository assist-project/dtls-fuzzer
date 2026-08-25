package se.uu.it.dtlsfuzzer.components.sul.core;

import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULAdapter;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsInputTransformer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class TlsSulRA
    implements
        AbstractSUL<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> {

    private static final Logger LOGGER = LogManager.getLogger();

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
        LOGGER.debug("Input PSymbolInstance: {}", in);
        TlsInput input = inputTransformer.fromTransformedInput(
            in.getBaseSymbol()
        );
        LOGGER.debug("Transformed TlsInput: {}", input);
        TlsOutput output = wrappedSul.step(input);
        LOGGER.debug("Received TlsOutput: {}", output);
        OutputSymbol base = new OutputSymbol(output.getName());
        return new PSymbolInstance(base);
    }

    @Override
    public SULConfig getSULConfig() {
        return wrappedSul.getSULConfig();
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
    public SULAdapter getSULAdapter() {
        return wrappedSul.getSULAdapter();
    }
}
