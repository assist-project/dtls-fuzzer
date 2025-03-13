package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;

public class TlsOutputBuilder implements OutputBuilder<TlsOutput> {

    public TlsOutputBuilder() {
        super();
    }

    @Override
    public TlsOutput buildOutput(String name) {
        return new TlsOutput(name);
    }
}
