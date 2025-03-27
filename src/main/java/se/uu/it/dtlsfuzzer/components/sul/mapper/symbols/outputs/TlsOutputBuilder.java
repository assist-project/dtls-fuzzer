package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;

public class TlsOutputBuilder extends  OutputBuilder<TlsOutput> {

    public TlsOutputBuilder() {
        super();
    }

    @Override
    public TlsOutput buildOutputExact(String name) {
        return new TlsOutput(name);
    }
}
