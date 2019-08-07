package se.uu.it.dtlsfuzzer.sut;

import net.automatalib.words.Word;

/**
 * @author Ramon Janssen
 */
public class NonDeterminismException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Word<?> oldOutput, newOutput, input;

	public NonDeterminismException(Word<?> input, Word<?> oldOutput,
			Word<?> newOutput) {
		this.input = input;
		this.oldOutput = oldOutput;
		this.newOutput = newOutput;
	}

	public NonDeterminismException(String message, Word<?> input,
			Word<?> oldOutput, Word<?> newOutput) {
		super(message);
		this.input = input;
		this.oldOutput = oldOutput;
		this.newOutput = newOutput;
	}

	/**
	 * The shortest cached output word which does not correspond with the new
	 * output
	 * 
	 * @return
	 */
	public Word<?> getOldOutput() {
		return this.oldOutput;
	}

	/**
	 * The full new output word
	 * 
	 * @return
	 */
	public Word<?> getNewOutput() {
		return this.newOutput;
	}

	/**
	 * The shortest sublist of the input word which still shows non-determinism
	 * 
	 * @return
	 */
	public Word<?> getShortestInconsistentInput() {
		int indexOfInconsistency = 0;
		while (oldOutput.getSymbol(indexOfInconsistency).equals(
				newOutput.getSymbol(indexOfInconsistency))) {
			indexOfInconsistency++;
		}
		return this.input.subWord(0, indexOfInconsistency);
	}

	@Override
	public String toString() {
		return "Non-determinism detected\nfull input:\n" + this.input
				+ "\nfull new output:\n" + this.newOutput + "\nold output:\n"
				+ this.oldOutput;
	}
}