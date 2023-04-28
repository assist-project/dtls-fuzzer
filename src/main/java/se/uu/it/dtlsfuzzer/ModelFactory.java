package se.uu.it.dtlsfuzzer;

import java.io.FileInputStream;
import java.io.IOException;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.InputModelData;
import net.automatalib.words.Alphabet;
import se.uu.it.dtlsfuzzer.sut.input.NameTlsInputMapping;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class ModelFactory {
    public static MealyMachine<?,TlsInput,?,TlsOutput> buildTlsModel(Alphabet<TlsInput> alphabet, String modelPath) throws IOException {
        NameTlsInputMapping definitions = new NameTlsInputMapping(alphabet);
        InputModelData<TlsInput, CompactMealy<TlsInput, TlsOutput>> result = MealyDotParser.parse(new CompactMealy.Creator<>(), new FileInputStream(modelPath), new TlsProcessor(definitions));
        return result.model;
    }
}
