package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;

import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;

public class TlsOutputBuilderRA implements OutputBuilder<PSymbolInstance> {
    public PSymbolInstance buildOutput(String name) {
        OutputSymbol baseSymbol = new OutputSymbol(name);
        return new PSymbolInstance(baseSymbol);
    }

}
