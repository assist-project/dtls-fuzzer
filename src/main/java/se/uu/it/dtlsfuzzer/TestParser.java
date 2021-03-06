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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;

/**
 * Reads tests from a file and writes them to a file using an alphabet.
 */

public class TestParser {
	private static final Logger LOGGER = LogManager.getLogger(TestParser.class);

	public TestParser() {
	}

	public void writeTest(Word<TlsInput> test, String PATH) throws IOException {
		File file = new File(PATH);
		file.createNewFile();
		try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
			for (TlsInput input : test) {
				pw.println(input.toString());
			}
		}
	}

	public Word<TlsInput> readTest(Alphabet<TlsInput> alphabet, String PATH)
			throws IOException {
		List<String> inputStrings = readTestStrings(PATH);
		Word<TlsInput> test = readTest(alphabet, inputStrings, true);
		return test;
	}

	public Word<TlsInput> readTest(Alphabet<TlsInput> alphabet,
			List<String> testInputStrings, boolean throwOnMissing) {
		Map<String, TlsInput> inputs = new LinkedHashMap<>();
		alphabet.stream().forEach(i -> inputs.put(i.toString(), i));
		Word<TlsInput> inputWord = Word.epsilon();
		for (String inputString : testInputStrings) {
			inputString = inputString.trim();
			if (!inputs.containsKey(inputString)) {
				if (throwOnMissing)
					throw new RuntimeException(
							"Input is missing from the alphabet " + inputString);
				else {
					LOGGER.warn("Input is missing from the alphabet "
							+ inputString);
					return null;
				}
			}
			inputWord = inputWord.append(inputs.get(inputString));
		}

		return inputWord;
	}

	/**
	 * Reads reset-separated tests (sequences of inputs). Inputs in a test can
	 * either be new-line- or space-separated.
	 */
	public List<Word<TlsInput>> readTests(Alphabet<TlsInput> alphabet,
			String PATH) throws IOException {
		List<String> inputStrings = readTestStrings(PATH);
		List<Word<TlsInput>> tests = new LinkedList<>();
		LinkedList<String> currentTestStrings = new LinkedList<>();
		for (String inputString : inputStrings) {
			if (inputString.equals("reset")) {
				Word<TlsInput> test = readTest(alphabet, currentTestStrings,
						false);
				if (test != null)
					tests.add(test);
				else {
					LOGGER.warn("Excluding invalid test " + currentTestStrings);
				}
				currentTestStrings.clear();
			} else {
				String[] spSepInputs = inputString.split("\\s");
				currentTestStrings.addAll(Arrays.asList(spSepInputs));
			}
		}
		if (!inputStrings.isEmpty()) {
			Word<TlsInput> test = readTest(alphabet, currentTestStrings, false);
			if (test != null) {
				tests.add(test);
			} else {
				LOGGER.warn("Excluding invalid test " + currentTestStrings);
			}
		}
		return tests;
	}

	private List<String> readTestStrings(String PATH) throws IOException {
		List<String> trace;
		trace = Files.readAllLines(Paths.get(PATH), StandardCharsets.US_ASCII);
		ListIterator<String> it = trace.listIterator();
		while (it.hasNext()) {
			String line = it.next();
			if (line.startsWith("#")) {
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
