package de.rub.nds.modelfuzzer.sut.io;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pfg666.dotparser.fsm.mealy.MealyProcessor;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;

public class TlsProcessor implements MealyProcessor<TlsInput, TlsOutput>{
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
	public TlsOutput processOutput(String output) {
		List<ProtocolMessage> messages = new ArrayList<ProtocolMessage>();
		List<String> messageStrings = Arrays.asList(output.split(","));
		messageStrings.stream().map(m -> SymbolicAlphabet.createWord(symbol))
		for (String message : output.split(",")) {
			SymbolicAlphabet.
		}
		new TlsOutput();
		return output.trim();
	}
}
