package se.uu.it.dtlsfuzzer;

import java.util.HashMap;
import java.util.Map;
import net.automatalib.commons.util.Pair;
import se.uu.it.dtlsfuzzer.sut.input.NameTlsInputMapping;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class TlsProcessor implements MealyDotParser.MealyInputOutputProcessor <TlsInput,TlsOutput> {
	private Map<String, TlsInput> cache;
	private NameTlsInputMapping definitions;

	public TlsProcessor(NameTlsInputMapping definitions) {
		cache = new HashMap<>();
		this.definitions = definitions;
	}

	public Pair<TlsInput,TlsOutput> processMealyInputOutput(String inputName, String outputName) {
		inputName = inputName.trim();
		if (!cache.containsKey(inputName)) {
			TlsInput tlsInput = definitions.getInput(inputName);
			if (tlsInput == null) {
				throw new RuntimeException("Input " + inputName
						+ " could not be found in the given mapping.\n "
						+ definitions.toString());
			}
			cache.put(inputName, definitions.getInput(inputName));
		}
		TlsInput input = cache.get(inputName);
		// FIXME Patchwork to work with models using the old output splitter (,). Should be removed.
		outputName = outputName.replaceAll("\\,", "|");
		outputName = outputName.replaceAll("WARNING\\|", "WARNING,");
		outputName = outputName.replaceAll("FATAL\\|", "FATAL,");

		TlsOutput output = new TlsOutput(outputName.trim());

		return Pair.of(input, output);
	}
}
