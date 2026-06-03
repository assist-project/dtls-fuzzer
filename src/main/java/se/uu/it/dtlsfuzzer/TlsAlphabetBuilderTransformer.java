package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class TlsAlphabetBuilderTransformer extends AlphabetBuilderTransformer<TlsInput, String> {

    public TlsAlphabetBuilderTransformer(AlphabetBuilderStandard<TlsInput> alphabetBuilderStandard) {
        super(alphabetBuilderStandard);
    }

    @Override
    public String toTransformedInput(TlsInput tlsInput) {
        return tlsInput.getName();
    }

    @Override
    public TlsInput fromTransformedInput(String s) {
        return null;
    }
}
