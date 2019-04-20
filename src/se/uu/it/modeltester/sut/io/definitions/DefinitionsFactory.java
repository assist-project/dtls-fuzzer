package se.uu.it.modeltester.sut.io.definitions;

import net.automatalib.words.Alphabet;
import se.uu.it.modeltester.sut.io.TlsInput;

public class DefinitionsFactory {
	
	public static Definitions generateDefinitions(Alphabet<TlsInput> alphabet) {
		Definitions defs = new Definitions();
		for (TlsInput input : alphabet) {
			defs.addInputDefinition(input.toString(), input);
		}
		return defs;
	}
}
