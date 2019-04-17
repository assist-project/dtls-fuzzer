package de.rub.nds.modelfuzzer.learn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;

import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;

public class StateMachine {
	private static final Logger LOG = Logger.getLogger(StateMachine.class
			.getName());

	private MealyMachine<?, TlsInput, ?, TlsOutput> mealyMachine;

	private Alphabet<TlsInput> alphabet;

	public StateMachine(MealyMachine<?, TlsInput, ?, TlsOutput> mealyMachine, Alphabet<TlsInput> alphabet) {
		this.mealyMachine = mealyMachine;
		this.alphabet = alphabet;
	}

	public MealyMachine<?, TlsInput, ?, TlsOutput>  getMealyMachine() {
		return mealyMachine;
	}

	public void setMealyMachine(MealyMachine<?, TlsInput, ?, TlsOutput>  mealyMachine) {
		this.mealyMachine = mealyMachine;
	}

	public Alphabet<TlsInput> getAlphabet() {
		return alphabet;
	}

	public void setAlphabet(Alphabet<TlsInput> alphabet) {
		this.alphabet = alphabet;
	}

	/**
	 * Exports the hypothesis to the supplied file and generates a corresponding
	 * viewable .pdf model.
	 */
	public void export(File graphFile, boolean generatePdf) {
		try {
			graphFile.createNewFile();
			export(new FileWriter(graphFile));
			if (generatePdf) {
				Runtime.getRuntime().exec(
						"dot -Tpdf -O " + graphFile.getAbsolutePath());
			}
		} catch (IOException e) {
			LOG.info("Could not export model!");
		}
	}

	public void export(Writer writer) {
		try {
			GraphDOT.write(mealyMachine, alphabet, writer);
			writer.close();
		} catch (IOException e) {
			LOG.info("Could not export model!");
		}
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		export(sw);
		return sw.toString();
	}
}
