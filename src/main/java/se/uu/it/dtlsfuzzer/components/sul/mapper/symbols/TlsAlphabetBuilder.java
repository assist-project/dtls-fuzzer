package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import net.automatalib.alphabet.Alphabet;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsAlphabetPojoXml;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class TlsAlphabetBuilder extends AlphabetBuilderStandard<TlsInput> {

    public TlsAlphabetBuilder() {
        super( new AlphabetSerializerXml<>(
                TlsInput.class,
                TlsAlphabetPojoXml.class
            ));
    }

    public Alphabet<TlsInput> build(LearnerConfig learnerConfig) {
        Alphabet<TlsInput> alphabet = super.build(learnerConfig);
        validateParams(alphabet);
        return alphabet;
    }

    /*
     * Validates that all parameters configured for each input in the alphabet
     * are supported by that input.
     *
     * @param alphabet the alphabet to validate
     * @throws IllegalArgumentException if an unsupported parameter is found
     */
    private void validateParams(Alphabet<TlsInput> alphabet) {
        for (var input : alphabet) {
            var supportedParams = input.getSupportedParams();
            for (var param : input.getParams()) {
                if (!supportedParams.contains(param)) {
                    throw new IllegalArgumentException(String.format("Input %s contains unsupported parameter %s. Supported parameters: %s",
                            input.getName(),
                            param,
                            supportedParams));
                }
            }
        }
    }
}
