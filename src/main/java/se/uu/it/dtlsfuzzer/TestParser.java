package se.uu.it.dtlsfuzzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;

/**
 * Reads tests from a file and writes them to a file using an alphabet.
 *
 * Mutations of an input are encoded in the following way: {@literal @} + input
 * name + JSON encoding of the mutations.
 *
 */

public class TestParser {
	private static final Logger LOGGER = LogManager.getLogger(TestParser.class);

	public TestParser() {
	}

	public void writeTest(Word<TlsInput> test, String PATH) throws IOException {
		File file = new File(PATH);
		writeTest(test, file);
	}

	public void writeTest(Word<TlsInput> test, File file) throws IOException {
		file.createNewFile();
		try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
			for (TlsInput input : test) {
				pw.println(input.toString());
			}
		}
	}

	public Word<TlsInput> readTest(Alphabet<TlsInput> alphabet, String PATH) throws IOException {
		List<String> inputStrings = readTestStrings(PATH);
		Word<TlsInput> test = readTest(alphabet, inputStrings);
		return test;
	}

	public Word<TlsInput> readTest(Alphabet<TlsInput> alphabet, List<String> testInputStrings) {
		Map<String, TlsInput> inputs = new LinkedHashMap<>();
		alphabet.stream().forEach(i -> inputs.put(i.toString(), i));
		Word<TlsInput> inputWord = Word.epsilon();
		for (String inputString : testInputStrings) {
			inputString = inputString.trim();
			if (!inputs.containsKey(inputString)) {
				throw new RuntimeException("Input \"" + inputString + "\" is missing from the alphabet ");
			}
			inputWord = inputWord.append(inputs.get(inputString));
		}

		return inputWord;
	}

	/**
	 * Reads from a file reset-separated test queries. It stops reading once it
	 * reaches the EOF or an empty line. A non-empty line may contain:
	 * <ul>
	 * <li>reset - marking the end of the current test, and the beginning of a new
	 * test</li>
	 * <li>space-separated regular inputs and resets</li>
	 * <li>a single mutated input (starts with @)</li>
	 * <li>commented line (starts with # or !)</li>
	 * </ul>
	 */
	public List<Word<TlsInput>> readTests(Alphabet<TlsInput> alphabet, String PATH) throws IOException {
		List<String> inputStrings = readTestStrings(PATH);
		List<String> flattenedInputStrings = inputStrings.stream()
				.map(i -> i.startsWith("@") ? new String[] { i } : i.split("\\s+")).flatMap(a -> Arrays.stream(a))
				.collect(Collectors.toList());

		List<Word<TlsInput>> tests = new LinkedList<>();
		LinkedList<String> currentTestStrings = new LinkedList<>();
		for (String inputString : flattenedInputStrings) {
			if (inputString.equals("reset")) {
				tests.add(readTest(alphabet, currentTestStrings));
				currentTestStrings.clear();
			} else {
				currentTestStrings.add(inputString);
			}
		}
		if (!inputStrings.isEmpty()) {
			tests.add(readTest(alphabet, currentTestStrings));
		}
		return tests;
	}

	private List<String> readTestStrings(String PATH) throws IOException {
		List<String> trace;
		trace = Files.readAllLines(Paths.get(PATH), StandardCharsets.US_ASCII);
		ListIterator<String> it = trace.listIterator();
		while (it.hasNext()) {
			String line = it.next();
			if (line.startsWith("#") || line.startsWith("!")) {
				it.remove();
			} else {
				if (line.isEmpty()) {
					it.remove();
					while (it.hasNext()) {
						it.next();
						it.remove();
					}
				} else {
				}
			}
		}
		return trace;
	}
}
