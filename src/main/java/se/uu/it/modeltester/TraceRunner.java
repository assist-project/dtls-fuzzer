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
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class TraceRunner {
	private static final Logger LOGGER = LogManager.getLogger(TraceRunner.class);
	private Map<String, TlsInput> inputs;
	private String path;
	private int repeats;
	private SULOracle<TlsInput, TlsOutput> sulOracle;
	
	public TraceRunner(ModelBasedTesterConfig config, Alphabet<TlsInput> alphabet, SULOracle<TlsInput, TlsOutput> sutOracle) {
		inputs = new HashMap<>();
		alphabet.stream().forEach(i -> this.inputs.put(i.getName(), i));
		path = config.getTrace();
		this.sulOracle = sutOracle;
		repeats = config.getTimes();
	}
	
	public void runTrace() throws IOException {
		List<String> inputStrings = readTraceStrings(path);
		Word<TlsInput> trace = Word.epsilon();
		for (String inputString : inputStrings) {
			if (!inputs.containsKey(inputString)) {
				throw new RuntimeException("Input is missing from the alphabet "+ inputString);
			}
			trace = trace.append(inputs.get(inputString));
		}
		Map<Word<TlsOutput>, Integer> answerMap = new LinkedHashMap<>();
		for (int i=0; i<repeats; i++) {
			Word<TlsOutput> answer = sulOracle.answerQuery(trace);
			if (!answerMap.containsKey(answer)) {
				answerMap.put(answer, 1);
			}
			else {
				answerMap.put(answer, answerMap.get(answer)+1);
			}
		}
		LOGGER.error("Inputs: " + trace + "\n");
		for (Word<TlsOutput> answer : answerMap.keySet()) {
			LOGGER.error(answerMap.get(answer) + " times outputs: " + answer.toString() + "\n");
		}
	}
	
	private List<String> readTraceStrings(String PATH) throws IOException {
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
					 System.out.println();
				 }
			}
		}
		return trace;
	}
}
