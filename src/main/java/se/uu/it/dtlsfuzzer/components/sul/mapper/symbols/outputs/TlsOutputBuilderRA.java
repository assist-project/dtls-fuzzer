package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;

public class TlsOutputBuilderRA extends OutputBuilder<PSymbolInstance> {

    @Override
    public PSymbolInstance buildOutputExact(String name) {
        return new PSymbolInstance(new OutputSymbol(name));
    }

}
