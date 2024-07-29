package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposerRA;
import de.learnlib.ralib.words.PSymbolInstance;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilderRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputCheckerRA;

public class DtlsMapperComposerRA
        extends MapperComposerRA<PSymbolInstance, TlsProtocolMessage, TlsExecutionContextRA, TlsState> {
    public DtlsMapperComposerRA(DtlsInputMapperRA inputMapper, DtlsOutputMapperRA outputMapper) {
        super(inputMapper, outputMapper);
    }

    @Override
    public TlsOutputCheckerRA getOutputChecker() {
        return (TlsOutputCheckerRA) super.getOutputChecker();
    }

    @Override
    public TlsOutputBuilderRA getOutputBuilder() {
        return (TlsOutputBuilderRA) super.getOutputBuilder();
    }
}
