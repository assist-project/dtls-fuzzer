package se.uu.it.dtlsfuzzer.learn;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.TestRunner;
import se.uu.it.dtlsfuzzer.TestRunnerResult;
import se.uu.it.dtlsfuzzer.sut.NonDeterminismException;

/**
 * Adding CE sanitizing at the SULOracle level allows us to avoid having to
 * restart the whole testing process due to spurious counterexamples. It does
 * come at the cost of doing the output comparison twice, but that cost is
 * insignificant in the context of learning DTLS.
 */
public class CESanitizingSULOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, Word<O>>, I, O>
		implements
			MealyMembershipOracle<I, O> {

	private static final Logger LOGGER = LogManager
			.getLogger(CESanitizingSULOracle.class);

	// x times cereruns
	private static final int PROBABILISTIC_MULTIPLIER = 10;
	private static final double PROBABILISTIC_THRESHOLD = 0.8;

	private int ceReruns;

	private MealyMembershipOracle<I, O> sulOracle;
	private Supplier<A> automatonProvider;
	private boolean probabilisticSanitization;

	private PrintWriter log;

	public CESanitizingSULOracle(int ceReruns,
			MealyMembershipOracle<I, O> sulOracle,
			Supplier<A> automatonProvider, boolean probabilisticSanitization,
			Writer log) {
		this.sulOracle = sulOracle;
		this.automatonProvider = automatonProvider;
		this.ceReruns = ceReruns;
		this.probabilisticSanitization = probabilisticSanitization;
		this.log = new PrintWriter(log);
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		for (Query<I, Word<O>> q : queries) {
			processQuery(q);
		}
	}

	public void processQuery(Query<I, Word<O>> q) {
		Word<O> originalOutput = sulOracle.answerQuery(q.getInput());
		A automaton = automatonProvider.get();
		Word<O> autOutput = automaton.computeOutput(q.getInput());
		Word<O> returnedOutput = null;

		// ok, we have what appears to be a counterexample
		if (!originalOutput.equals(autOutput)) {
			LOGGER.info("Verifying potential counterexample");
			// we generate a checked output, that is, an output which has been
			// confirmed
			returnedOutput = getCheckedOutput(q.getInput(), originalOutput,
					autOutput);
		}
		// no counterexample, making it safe to return the original output
		else {
			returnedOutput = originalOutput;
		}

		q.answer(returnedOutput.suffix(q.getSuffix().length()));
	}

	private Word<O> getCheckedOutput(Word<I> input, Word<O> originalOutput,
			Word<O> autOutput) {
		TestRunnerResult<I, O> result = TestRunner.runTest(input, ceReruns,
				sulOracle);
		Iterator<Word<O>> outputIter = result.getGeneratedOutputs().keySet()
				.iterator();
		Word<O> checkedOutput = null;
		if (result.getGeneratedOutputs().size() > 1) {
			log.println("Non determinism in CE sanitization");
			log.write(result.toString());
			log.flush();
			if (!probabilisticSanitization) {
				throw new NonDeterminismException(input, outputIter.next(),
						outputIter.next()).makeCompact();
			} else {
				checkedOutput = getProbabilisticOutput(input);
			}
		} else {
			checkedOutput = outputIter.next();
		}
		if (!checkedOutput.equals(originalOutput)) {
			log.println("Output changed following CE verification");
			log.println(result.toString());
			log.println("Original output: " + originalOutput);
			log.println("New CE status: " + autOutput.equals(checkedOutput));
			log.flush();
		}
		return checkedOutput;
	}

	private Word<O> getProbabilisticOutput(Word<I> input) {
		log.println("Performing probabilistic sanitization");
		int probabilisticReruns = ceReruns * PROBABILISTIC_MULTIPLIER;
		TestRunnerResult<I, O> result = TestRunner.runTest(input,
				probabilisticReruns, sulOracle);
		log.println(result.toString());
		Entry<Word<O>, Integer> mostCommonEntry = result.getGeneratedOutputs()
				.entrySet().stream()
				.max(new Comparator<Map.Entry<Word<O>, Integer>>() {
					public int compare(Entry<Word<O>, Integer> arg0,
							Entry<Word<O>, Integer> arg1) {
						return arg0.getValue().compareTo(arg1.getValue());
					}
				}).get();
		double likelyhood = (double) (mostCommonEntry.getValue() / probabilisticReruns);
		log.println("Most common entry likelyhood: " + likelyhood);
		if (likelyhood > PROBABILISTIC_THRESHOLD) {
			return mostCommonEntry.getKey();
		} else {
			Iterator<Word<O>> outputIter = result.getGeneratedOutputs()
					.keySet().iterator();
			// TODO NonDeterminismException should carry multiple outputs
			throw new NonDeterminismException(input, outputIter.next(),
					outputIter.next());
		}
	}

}
