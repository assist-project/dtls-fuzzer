package de.rub.nds.modelfuzzer.sut.io;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
		List<ProtocolMessage> outputMessages = new ArrayList<>();
		for (String outputString : output.split(",")) {
			ProtocolMessage message;
			try {
				message = (ProtocolMessage) Class.forName(ProtocolMessage.class.getPackage().getName() + "." + outputString)
				.getConstructor()
				.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Could not instantiate output: " + outputString);
			} 
			outputMessages.add(message);
			
		}
		return new TlsOutput(outputMessages, Collections.emptyList(), null);
	}
}
