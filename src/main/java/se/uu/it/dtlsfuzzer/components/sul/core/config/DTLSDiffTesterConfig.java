package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;
import net.automatalib.alphabet.Alphabet;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsAlphabetPojoXml;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class DTLSDiffTesterConfig extends DiffTesterConfigStandard {

    public Alphabet<TlsInput> getTlsAlphabet() {
        AlphabetBuilder<TlsInput> alphabetBuilder = new AlphabetBuilderStandard<>(
            new AlphabetSerializerXml<>(TlsInput.class, TlsAlphabetPojoXml.class));

        return alphabetBuilder.build(new LearnerConfig() {
            @Override
            public String getAlphabetFilename() {
                return alphabetFilePath;
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Alphabet<String> getAlphabet() {
        return (Alphabet<String>) (Alphabet<?>) getTlsAlphabet();
    }
}
