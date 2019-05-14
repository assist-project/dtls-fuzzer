package se.uu.it.modeltester;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.modeltester.mutate.JsonMutationParser;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.sut.io.TlsInput;

public class TestParser {
	private static final Logger LOGGER = LogManager.getLogger(TestParser.class);
	private Map<String, TlsInput> inputs;

	public TestParser(Alphabet<TlsInput> alphabet) {
		inputs = new LinkedHashMap<>();
		alphabet.stream().forEach(i -> this.inputs.put(i.getName(), i));
	}
	
	public void writeTest(Word<TlsInput> test, String PATH) throws IOException {
		
	}
	
	public Word<TlsInput> readTest(String PATH) throws IOException{
		List<String> inputStrings = readTestStrings(PATH);
		Word<TlsInput> inputWord = Word.epsilon();
		for (String inputString : inputStrings) {
			if (inputString.startsWith("@")) {
				String mutatedInputString = inputString.substring(1, inputString.indexOf("["));
				if (!inputs.containsKey(mutatedInputString)) {
					throw new RuntimeException("Input is missing from the alphabet "+ inputString);
				}
				String mutationsJsonString = inputString.substring(inputString.indexOf("["), inputString.length());
				Mutation [] mutation = JsonMutationParser.getInstance().deserialize(mutationsJsonString);
				
				
			} else {
				if (!inputs.containsKey(inputString)) {
					throw new RuntimeException("Input is missing from the alphabet "+ inputString);
				}
				inputWord = inputWord.append(inputs.get(inputString));
			}
		}
		
		return inputWord;
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
