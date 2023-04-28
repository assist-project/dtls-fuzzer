package se.uu.it.dtlsfuzzer;

import java.io.IOException;
import java.io.InputStream;
import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;

public class MealyDotParser {
    public static <I,O, A extends MutableMealyMachine<?, I, ?, O>> InputModelData<I, A> parse(AutomatonCreator<A, I> creator, InputStream inputStream, MealyInputOutputProcessor <I,O> processor) throws IOException {
        InputModelDeserializer<I, A> parser = DOTParsers.mealy(creator, (map)
                -> {
                    Pair<String,String> ioStringPair = DOTParsers.DEFAULT_MEALY_EDGE_PARSER.apply(map);
                    Pair<I,O> ioPair = processor.processMealyInputOutput(ioStringPair.getFirst(), ioStringPair.getSecond());
                    return ioPair;
                });
        return parser.readModel(inputStream);
    }

    public static interface MealyInputOutputProcessor <I,O> {
        Pair<I,O> processMealyInputOutput(String inputName, String outputName);
    }
}
