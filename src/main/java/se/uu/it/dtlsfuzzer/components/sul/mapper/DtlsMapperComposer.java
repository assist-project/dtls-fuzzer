package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;

import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class DtlsMapperComposer
        extends MapperComposer<TlsInput, TlsOutput, TlsProtocolMessage, TlsExecutionContext, TlsState> {
    public DtlsMapperComposer(DtlsInputMapper inputMapper, DtlsOutputMapper outputMapper) {
        super(inputMapper, outputMapper);
    }

    @Override
    public DtlsOutputChecker getOutputChecker() {
        return (DtlsOutputChecker) super.getOutputChecker();
    }

    @Override
    public DtlsOutputBuilder getOutputBuilder() {
        return (DtlsOutputBuilder) super.getOutputBuilder();
    }
}
