package se.uu.it.modeltester;

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
import se.uu.it.modeltester.mutate.JsonMutationParser;
import se.uu.it.modeltester.mutate.MutatedTlsInput;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.sut.io.TlsInput;

/**
 * Reads tests from a file and writes them to a file using an alphabet.
 * 
 * Mutations of an input are encoded in the following way:
 * {@literal @} + input name + JSON encoding of the mutations
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
				if (input instanceof MutatedTlsInput) {
					StringBuilder builder = new StringBuilder();
					TlsInput originalInput = ((MutatedTlsInput)input).getInput();
					List<Mutation<?>> mutations = ((MutatedTlsInput)input).getMutations();
					String jsonMutations = JsonMutationParser.getInstance().serialize(mutations.toArray(new Mutation[mutations.size()]));
					((MutatedTlsInput)input).getInput();
					builder
					.append("@")
					.append(originalInput.toString())
					.append(jsonMutations);
					pw.println(builder.toString());
				} else {
					pw.println(input.toString());
				}
			}
		} 
	}
	
	public Word<TlsInput> readTest(Alphabet<TlsInput> alphabet, String PATH) throws IOException{
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
			if (inputString.startsWith("@")) {
				String mutatedInputString = inputString.substring(1, inputString.indexOf("["));
				if (!inputs.containsKey(mutatedInputString)) {
					throw new RuntimeException("Mutated input is missing from the alphabet "+ mutatedInputString);
				}
				String mutationsJsonString = inputString.substring(inputString.indexOf("["), inputString.length());
				Mutation<?> [] mutations = JsonMutationParser.getInstance().deserialize(mutationsJsonString);
				MutatedTlsInput mutatedInput = new MutatedTlsInput(inputs.get(mutatedInputString), Arrays.asList(mutations));
				inputWord = inputWord.append(mutatedInput);
			} else {
				if (!inputs.containsKey(inputString)) {
					throw new RuntimeException("Input is missing from the alphabet "+ inputString);
				}
				inputWord = inputWord.append(inputs.get(inputString));
			}
		}
		
		return inputWord;
	}
	
	/**
	 * Reads reset-seperated tests
	 */
	public List<Word<TlsInput>> readTests(Alphabet<TlsInput> alphabet, String PATH) throws IOException{
		List<String> inputStrings = readTestStrings(PATH);
		List<Word<TlsInput>> tests = new LinkedList<>();
		LinkedList<String> currentTestStrings = new LinkedList<>();
		for (String inputString : inputStrings) {
			if (inputString.equals("reset")) {
				tests.add(readTest(alphabet, currentTestStrings));
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
		while(it.hasNext()) {
			String line = it.next();
			if (line.startsWith("#") || line.startsWith("!")) {
				it.remove();
			} else {
				 if ( line.isEmpty()) {
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
