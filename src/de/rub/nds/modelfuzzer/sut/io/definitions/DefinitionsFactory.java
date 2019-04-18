package de.rub.nds.modelfuzzer.sut.io.definitions;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import net.automatalib.words.Alphabet;

public class DefinitionsFactory {
	
	public static Definitions generateDefinitions(Alphabet<TlsInput> alphabet) {
		Definitions defs = new Definitions();
		for (TlsInput input : alphabet) {
			defs.addInputDefinition(input.toString(), input);
		}
		return defs;
	}
}
