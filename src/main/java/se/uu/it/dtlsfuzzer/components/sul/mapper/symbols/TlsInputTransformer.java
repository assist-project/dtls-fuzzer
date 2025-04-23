package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.ParameterizedSymbol;
import java.util.LinkedHashMap;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.RAOutputSymbol;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class TlsInputTransformer
    extends AlphabetBuilderTransformer<TlsInput, ParameterizedSymbol> {

    private LinkedHashMap<ParameterizedSymbol, TlsInput> translationMap =
        new LinkedHashMap<>();

    public TlsInputTransformer(
        AlphabetBuilderStandard<TlsInput> alphabetBuilder
    ) {
        super(alphabetBuilder);
    }

    @Override
    public ParameterizedSymbol toTransformedInput(TlsInput ri) {
        if (ri instanceof RAOutputSymbol) {
            ParameterizedSymbol translated = new OutputSymbol(ri.getName());
            // Don't put output symbols in the translation map because we don't want to feed them as inputs.
            return translated;
        } else {
            ParameterizedSymbol translated = new InputSymbol(ri.getName());
            translationMap.put(translated, ri);
            return translated;
        }
    }

    @Override
    public TlsInput fromTransformedInput(ParameterizedSymbol ti) {
        return translationMap.get(ti);
    }
}
