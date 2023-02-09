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
	private Word<?> oldOutput, newOutput, input, precedingInput;

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
	 * Sets the preceding
	 */
	public void setPrecedingInput(Word<?> precedingInput) {
		this.precedingInput = precedingInput;
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
		return this.input.subWord(0, indexOfInconsistency + 1);
	}

	// TODO this is a lazy implementation.
	public NonDeterminismException makeCompact() {
		this.input = getShortestInconsistentInput();
		this.oldOutput = this.oldOutput.prefix(input.length());
		this.newOutput = this.newOutput.prefix(input.length());
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Non-determinism detected\n");
		sb.append("full input:\n");
		sb.append(input);
		sb.append("\nfull new output:\n");
		sb.append(newOutput);
		sb.append("\nold output:\n");
		sb.append(oldOutput);
		if (precedingInput != null)
			sb.append("\npreceding input:\n");
		sb.append(precedingInput);
		return sb.toString();
	}
}
