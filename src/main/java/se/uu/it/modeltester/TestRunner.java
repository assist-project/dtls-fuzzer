package se.uu.it.modeltester;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.oracles.SULOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.modeltester.config.ModelBasedTesterConfig;
import se.uu.it.modeltester.config.TestRunnerConfig;
import se.uu.it.modeltester.mutate.JsonMutationParser;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class TestRunner {
	private static final Logger LOGGER = LogManager.getLogger(TestRunner.class);
	private Map<String, TlsInput> inputs;
	private String path;
	private int repeats;
	private SULOracle<TlsInput, TlsOutput> sulOracle;
	
	public TestRunner(TestRunnerConfig config, Alphabet<TlsInput> alphabet, SULOracle<TlsInput, TlsOutput> sutOracle) {
		inputs = new HashMap<>();
		alphabet.stream().forEach(i -> this.inputs.put(i.getName(), i));
		path = config.getTest();
		this.sulOracle = sutOracle;
		repeats = config.getTimes();
	}
	
	public TestRunnerResult runTrace() throws IOException {
		List<String> inputStrings = readTestStrings(path);
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
		Map<Word<TlsOutput>, Integer> answerMap = new LinkedHashMap<>();
		for (int i=0; i<repeats; i++) {
			Word<TlsOutput> answer = sulOracle.answerQuery(inputWord);
			if (!answerMap.containsKey(answer)) {
				answerMap.put(answer, 1);
			}
			else {
				answerMap.put(answer, answerMap.get(answer)+1);
			}
		}
		LOGGER.error("Inputs: " + inputWord + "\n");
		for (Word<TlsOutput> answer : answerMap.keySet()) {
			LOGGER.error(answerMap.get(answer) + " times outputs: " + answer.toString() + "\n");
		}
		
		return new TestRunnerResult(inputWord, answerMap);
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
