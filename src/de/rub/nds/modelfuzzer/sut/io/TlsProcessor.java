package de.rub.nds.modelfuzzer.sut.io;


import java.util.HashMap;
import java.util.Map;

import com.pfg666.dotparser.fsm.mealy.MealyProcessor;

public class TlsProcessor implements MealyProcessor<TlsInput, String>{
	private Map<String, TlsInput> cache;
	
	public TlsProcessor() {
		cache = new HashMap<>();
	}

	@Override
	public TlsInput processInput(String input) {
		if (!cache.containsKey(input)) {
			cache.put(input, SymbolicAlphabet.createWord(TlsSymbol.valueOf(input.trim())));
		}
		return cache.get(input);
	}

	@Override
	public String processOutput(String output) {
		return output.trim();
	}
}
