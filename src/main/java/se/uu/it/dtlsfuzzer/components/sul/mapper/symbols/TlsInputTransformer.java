package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.ParameterizedSymbol;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class TlsInputTransformer
    extends AlphabetBuilderTransformer<TlsInput, ParameterizedSymbol> {

    public TlsInputTransformer(
        AlphabetBuilderStandard<TlsInput> alphabetBuilder
    ) {
        super(alphabetBuilder);
    }

    @Override
    public ParameterizedSymbol toTransformedInput(TlsInput ri) {
        // FIXME: This will need to be updated to handle output symbols.
        // Probably by adding a TlsInput subclass that is actually a TlsOutput.
        // The alphabet files might need to be different to not break mealy learning.
        return new InputSymbol(ri.getName());
    }

    @Override
    public TlsInput fromTransformedInput(ParameterizedSymbol ti) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
            "Unimplemented method 'fromTransformedInput'"
        );
    }
}
