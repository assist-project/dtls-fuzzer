package se.uu.it.dtlsfuzzer.learner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class StateMachine {
	private static final Logger LOG = Logger.getLogger(StateMachine.class
			.getName());

	private MealyMachine<?, TlsInput, ?, TlsOutput> mealyMachine;

	private Alphabet<TlsInput> alphabet;

	public StateMachine(MealyMachine<?, TlsInput, ?, TlsOutput> mealyMachine,
			Alphabet<TlsInput> alphabet) {
		this.mealyMachine = mealyMachine;
		this.alphabet = alphabet;
	}

	public MealyMachine<?, TlsInput, ?, TlsOutput> getMealyMachine() {
		return mealyMachine;
	}

	public void setMealyMachine(
			MealyMachine<?, TlsInput, ?, TlsOutput> mealyMachine) {
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
	
	/**
	 * Creates a low level copy of the state machine.
	 */
	public StateMachine copy() {
		FastMealy<TlsInput,TlsOutput> mealyCopy = new FastMealy<>(alphabet);
		AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, mealyMachine, alphabet, mealyCopy);
		return new StateMachine(mealyCopy, new ListAlphabet<TlsInput>(new ArrayList<>(alphabet)));
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		export(sw);
		return sw.toString();
	}
}
