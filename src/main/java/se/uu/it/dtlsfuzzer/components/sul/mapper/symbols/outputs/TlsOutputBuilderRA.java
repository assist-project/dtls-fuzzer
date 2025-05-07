package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;

public class TlsOutputBuilderRA extends OutputBuilder<PSymbolInstance> {

    @Override
    public PSymbolInstance buildOutputExact(String name) {
        return new PSymbolInstance(new OutputSymbol(name));
    }

}
