package se.uu.it.dtlsfuzzer.sut;

import net.automatalib.words.Word;

/**
 * <pre>
 * Copied from <a href="https://gitlab.science.ru.nl/ramonjanssen/basic-learning/">basic-learning</a>.
 * </pre>
 *
 * Contains the full input for which non-determinism was observed, as well as
 * the full new output and the (possibly shorter) old output with which it
 * disagrees
 *
 * @author Ramon Janssen
 */
public class CacheInconsistencyException extends NonDeterminismException {

    /**
     *
     */
    private static final long serialVersionUID = 6532386093138639923L;

    public CacheInconsistencyException(Word<?> input, Word<?> oldOutput,
            Word<?> newOutput) {
        super(input, oldOutput, newOutput);
    }

    public CacheInconsistencyException(String message, Word<?> input,
            Word<?> oldOutput, Word<?> newOutput) {
        super(message, input, oldOutput, newOutput);
    }

}
