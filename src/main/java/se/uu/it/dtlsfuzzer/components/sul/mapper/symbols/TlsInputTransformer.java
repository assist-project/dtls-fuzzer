package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.RAOutputSymbol;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

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
        DataType[] types = ri.getParams().stream()
                .map(TlsParamRA::getDataType)
                .toArray(DataType[]::new);
        if (ri instanceof RAOutputSymbol) {
            ParameterizedSymbol translated = new OutputSymbol(ri.getName(), types);
            LOGGER.debug("Was OutputSymbol {}, added to translation map", translated);
            translationMap.put(translated, ri);
            return translated;
        } else {
            ParameterizedSymbol translated = new InputSymbol(ri.getName(), types);
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

    public RAOutputSymbol toRAOutputSymbol(TlsOutput output) {
        for (var e : translationMap.entrySet()) {
            ParameterizedSymbol ri = e.getKey();
            if (ri instanceof OutputSymbol && ri.getName().equals(output.getName())) {
                return (RAOutputSymbol) e.getValue();
            }
        }
        return null;
    }
}
