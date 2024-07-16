package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilder;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class DtlsMapperComposer
        extends MapperComposer<TlsInput, TlsOutput, TlsProtocolMessage, TlsExecutionContext, TlsState> {
    public DtlsMapperComposer(DtlsInputMapper inputMapper, DtlsOutputMapper outputMapper) {
        super(inputMapper, outputMapper);
    }

    @Override
    public TlsOutputChecker getOutputChecker() {
        return (TlsOutputChecker) super.getOutputChecker();
    }

    @Override
    public TlsOutputBuilder getOutputBuilder() {
        return (TlsOutputBuilder) super.getOutputBuilder();
    }
}
