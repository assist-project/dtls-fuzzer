package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.ParameterizedSymbol;
import java.util.LinkedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.RAOutputSymbol;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class TlsInputTransformer
    extends AlphabetBuilderTransformer<TlsInput, ParameterizedSymbol> {

    private static final Logger LOGGER = LogManager.getLogger();

    private LinkedHashMap<ParameterizedSymbol, TlsInput> translationMap =
        new LinkedHashMap<>();

    public TlsInputTransformer(
        AlphabetBuilderStandard<TlsInput> alphabetBuilder
    ) {
        super(alphabetBuilder);
    }

    @Override
    public ParameterizedSymbol toTransformedInput(TlsInput ri) {
        LOGGER.debug("Transforming TlsInput: {} of class: {}", ri, ri.getClass());
        if (ri instanceof RAOutputSymbol) {
            ParameterizedSymbol translated = new OutputSymbol(ri.getName());
            LOGGER.debug("Was OutputSymbol {}, not added to translation map", translated);
            // Don't put output symbols in the translation map because we don't want to feed them as inputs.
            return translated;
        } else {
            ParameterizedSymbol translated = new InputSymbol(ri.getName());
            translationMap.put(translated, ri);
            LOGGER.debug("Was input symbol {}, added to translation map", translated);
            return translated;
        }
    }

    @Override
    public TlsInput fromTransformedInput(ParameterizedSymbol ti) {
        LOGGER.debug("Translation map: {}", translationMap);
        return translationMap.get(ti);
    }
}
