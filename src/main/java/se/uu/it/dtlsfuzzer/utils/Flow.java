package se.uu.it.dtlsfuzzer.utils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

public abstract class Flow<I,O,F extends Flow<I,O,F>> {
	private Word<I> inputWord;
	private Word<O> outputWord;
	/**
	 * Does it start from the initial state or is it just partial
	 */
	private boolean fromStart;

	public Flow() {
		super();
		this.inputWord = Word.epsilon();
		this.outputWord = Word.epsilon();
		this.fromStart = false;
	}

	public Flow(Word<I> input, Word<O> output, boolean fromStart) {
		super();
		this.inputWord = input;
		this.outputWord = output;
		this.fromStart = fromStart;
	}

	public Word<I> getInputWord() {
		return inputWord;
	}

	public Word<O> getOutputWord() {
		return outputWord;
	}

	public I getInput(int index) {
		return inputWord.getSymbol(index);
	}

	public O getOutput(int index) {
		return outputWord.getSymbol(index);
	}

	public Stream<Pair<I,O>> getInputOutputStream() {
		Stream.Builder<Pair<I,O>> builder = Stream.builder();
		for (int i=0; i<getLength(); i++) {
			builder.add(Pair.of(inputWord.getSymbol(i), outputWord.getSymbol(i)));
		}
		return builder.build();
	}

	public Iterable<Pair<I,O>> getInputOutputIterable() {
		return getInputOutputStream().collect(Collectors.toList());
	}

	public F append(I input, O output) {
		return build(inputWord.append(input), outputWord.append(output), fromStart);
	}

	public F concat(F other) {
		if (other.isfromStart() || !this.isfromStart()) {
			throw new RuntimeException("These flows cannot be concatenated since their fromStart state is inconsistent");
		}
		return build(inputWord.concat(other.getInputWord()), outputWord.concat(other.getOutputWord()), fromStart);
	}

	public F prefix(int length) {
		if (length > getLength()) {
			throw new RuntimeException("Requested prefix length greater than flow length");
		}
		return build(inputWord.prefix(length), outputWord.prefix(length), fromStart);
	}

	protected abstract F build(Word<I> input, Word<O> output, boolean fromStart);

	public boolean isfromStart() {
		return fromStart;
	}

	public int getLength() {
		return inputWord.length();
	}

	public String toCompactString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Flow: ");
		for (int i=0; i<getLength(); i++) {
			builder.append(inputWord.getSymbol(i) + "/" + outputWord.getSymbol(i)).append(' ');
		}
		return builder.toString();
	}

	public String toString() {
		return String.format("Flow: \n  inputs: %s\n  outputs: %s\n", inputWord, outputWord);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fromStart ? 1231 : 1237);
		result = prime * result + ((inputWord == null) ? 0 : inputWord.hashCode());
		result = prime * result + ((outputWord == null) ? 0 : outputWord.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Flow other = (Flow) obj;
		if (fromStart != other.fromStart)
			return false;
		if (inputWord == null) {
			if (other.inputWord != null)
				return false;
		} else if (!inputWord.equals(other.inputWord))
			return false;
		if (outputWord == null) {
			if (other.outputWord != null)
				return false;
		} else if (!outputWord.equals(other.outputWord))
			return false;
		return true;
	}

}
