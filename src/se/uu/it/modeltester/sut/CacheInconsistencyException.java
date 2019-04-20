package se.uu.it.modeltester.sut;

import net.automatalib.words.Word;

/**
 * <pre>Copied from <a href="https://gitlab.science.ru.nl/ramonjanssen/basic-learning/">basic-learning</a>.
 * </pre>
 * 
 * Contains the full input for which non-determinism was observed, as well as the full new output
 * and the (possibly shorter) old output with which it disagrees
 * 
 * @author Ramon Janssen
 */
public class CacheInconsistencyException extends RuntimeException {
	private final Word oldOutput, newOutput, input;
	
	public CacheInconsistencyException(Word input, Word oldOutput, Word newOutput) {
		this.input = input;
		this.oldOutput = oldOutput;
		this.newOutput = newOutput;
	}
	
	public CacheInconsistencyException(String message, Word input, Word oldOutput, Word newOutput) {
		super(message);
		this.input = input;
		this.oldOutput = oldOutput;
		this.newOutput = newOutput;
	}
	

	/**
	 * The shortest cached output word which does not correspond with the new output
	 * @return
	 */
	public Word getOldOutput() {
		return this.oldOutput;
	}
	
	/**
	 * The full new output word
	 * @return
	 */
	public Word getNewOutput() {
		return this.newOutput;
	}

	/**
	 * The shortest sublist of the input word which still shows non-determinism
	 * @return
	 */
	public Word getShortestInconsistentInput() {
	    int indexOfInconsistency = 0;
	    while (oldOutput.getSymbol(indexOfInconsistency).equals(newOutput.getSymbol(indexOfInconsistency))) {
	        indexOfInconsistency ++;
	    }
	    return this.input.subWord(0, indexOfInconsistency);
	}
	
	@Override
	public String toString() {
		return "Non-determinism detected\nfull input:\n" + this.input + "\nfull new output:\n" + this.newOutput + "\nold output:\n" + this.oldOutput;
	}
}