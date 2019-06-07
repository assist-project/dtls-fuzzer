package se.uu.it.modeltester.sut.io.definitions;

import net.automatalib.words.Alphabet;
import se.uu.it.modeltester.sut.io.TlsInput;

//TODO there is redundancy between definitions and just creating a map from strings/identifiers to TlsInputs
// definitions were meant to be used to define inputs in XML. 
// currently, we use a name field in the TlsInput for that purpose.
// either definitions should be removed altogether or they should be 
// used universally when associating inputs to some naming (in which case the name field should be removed from inputs)
public class DefinitionsFactory {

	public static Definitions generateDefinitions(Alphabet<TlsInput> alphabet) {
		Definitions defs = new Definitions();
		for (TlsInput input : alphabet) {
			defs.addInputDefinition(input.toString(), input);
		}
		return defs;
	}
}
