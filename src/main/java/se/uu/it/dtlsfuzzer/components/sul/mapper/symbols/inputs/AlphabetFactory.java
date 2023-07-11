package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializerException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.AlphabetProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;

public class AlphabetFactory implements AlphabetBuilder {
    public static final String DEFAULT_ALPHABET = "/default_alphabet.xml";

    @Override
    public Alphabet<AbstractInput> build(AlphabetProvider alphabetProvider) {
        InputStream stream = getAlphabetFileInputStream(alphabetProvider);
        try {
            List<AbstractInput> inputs = new ArrayList<AbstractInput>();
            Alphabet<TlsInput> tlsAlphabet = AlphabetSerializer.read(stream);
            for (TlsInput input : tlsAlphabet) {
               input.updateName();
            }
            tlsAlphabet.forEach(in -> inputs.add(in));
            return new ListAlphabet<>(inputs);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing alphabet file", e);
        }
    }

    @Override
    public InputStream getAlphabetFileInputStream(AlphabetProvider alphabetProvider) {
        InputStream alphaStream = null;
        String name = alphabetProvider.getAlphabetFilename();
        if (name == null) {
            alphaStream = AlphabetFactory.class
                    .getResourceAsStream(DEFAULT_ALPHABET);
        } else {
            try {
                alphaStream = new FileInputStream(name);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Error reading alphabet file", e);
            }
        }
        return alphaStream;
    }

    @Override
    public String getAlphabetFileExtension() {
        return ".xml";
    }

    @Override
    public void exportAlphabetToFile(String outputFileName, Alphabet<AbstractInput> alphabet)
            throws IOException, AlphabetSerializerException {
        List<TlsInput> tlsInputs = new ArrayList<TlsInput>();
        alphabet.forEach(i -> tlsInputs.add((TlsInput) i));
        try {
            AlphabetSerializer.write(new FileOutputStream(outputFileName), new ListAlphabet<TlsInput>(tlsInputs));
        } catch (Exception e) {
            throw new AlphabetSerializerException(e.toString());
        }
    }

}
