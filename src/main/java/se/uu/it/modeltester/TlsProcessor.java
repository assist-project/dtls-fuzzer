package se.uu.it.modeltester;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.pfg666.dotparser.fsm.mealy.MealyProcessor;

import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;
import se.uu.it.modeltester.sut.io.definitions.Definitions;

public class TlsProcessor implements MealyProcessor<TlsInput, TlsOutput>{
	private Map<String, TlsInput> cache;
	private Definitions definitions;
	
	public TlsProcessor(Definitions definitions) {
		cache = new HashMap<>();
		this.definitions = definitions;
	}

	@Override
	public TlsInput processInput(String inputName) {
		inputName = inputName.trim();
		if (!cache.containsKey(inputName)) {
			TlsInput tlsInput = definitions.getInputWithDefinition(inputName);
			if (tlsInput == null) {
				throw new RuntimeException("Input " + inputName + " could not be found in the given definitions.\n " + definitions.toString());
			}
			cache.put(inputName, definitions.getInputWithDefinition(inputName));
		}
		return cache.get(inputName);
	}

	@Override
	public TlsOutput processOutput(String output) {
		String[] trimmedOutputStrings = 
				Arrays.stream(output.split(","))
				.map(o -> o.trim())
				.toArray(String []::new);
		
		return new TlsOutput(trimmedOutputStrings);
	}
}
