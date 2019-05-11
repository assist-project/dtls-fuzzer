package se.uu.it.modeltester.test;

import java.lang.reflect.Field;

import net.automatalib.words.Word;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public abstract class Bug {
	private final BugType type;
	private Word<TlsInput> inputs;
	private Word<TlsOutput> expected;
	private Word<TlsOutput> actual;
	
	public Bug(BugType type, Word<TlsInput> inputs, Word<TlsOutput> expected, Word<TlsOutput> actual) {
		this.inputs = inputs;
		this.expected = expected;
		this.actual = actual;
		this.type = type;
	}
	
	public Word<TlsInput> getInputs() {
		return inputs;
	}

	public Word<TlsOutput> getExpected() {
		return expected;
	}

	public Word<TlsOutput> getActual() {
		return actual;
	}

	public BugType getType() {
		return type;
	}
	
	
	public void minimize() {
		Word<TlsOutput> commonPrefix = expected.longestCommonPrefix(actual);
		assert commonPrefix.size() < expected.size() && commonPrefix.size() < actual.size() && expected.size() == actual.size();
		this.expected = commonPrefix.append(expected.getSymbol(commonPrefix.size()));
		this.inputs = inputs.prefix(commonPrefix.size()+1);
		this.actual = commonPrefix.append(actual.getSymbol(commonPrefix.size()));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName()).append(": {");
		for (Field f : this.getClass().getDeclaredFields() ) {
			f.setAccessible(true);
			try {
				sb.append("   ").append(f.getName())
				.append(": ").append(f.get(this)).append("\n");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	
	
}
