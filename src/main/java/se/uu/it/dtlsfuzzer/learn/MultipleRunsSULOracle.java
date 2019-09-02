package se.uu.it.dtlsfuzzer.learn;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.TestRunner;
import se.uu.it.dtlsfuzzer.TestRunnerResult;
import se.uu.it.dtlsfuzzer.sut.NonDeterminismException;

/**
 * A membership oracle which executes each query multiple times in order to
 * handle non-determinism. In case the runs result in different outputs, it can
 * perform probabilistic sanitization. This entails running the query many
 * times, and computing the answer with the highest likelihood.
 * 
 * If the likelihood is greater than a threshold the answer is returned,
 * otherwise an exception is thrown.
 */
public class MultipleRunsSULOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, Word<O>>, I, O>
		implements
			MealyMembershipOracle<I, O> {

	private static final Logger LOGGER = LogManager
			.getLogger(MultipleRunsSULOracle.class);

	// x times cereruns
	private static final int PROBABILISTIC_MULTIPLIER = 10;
	private static final double PROBABILISTIC_THRESHOLD = 0.8;

	private int runs;

	protected MealyMembershipOracle<I, O> sulOracle;
	private boolean probabilisticSanitization;

	protected PrintWriter log;

	public MultipleRunsSULOracle(int runs,
			MealyMembershipOracle<I, O> sulOracle,
			boolean probabilisticSanitization, Writer log) {
		this.sulOracle = sulOracle;
		this.runs = runs;
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
		Word<O> output = getMultipleRunOutput(q.getInput());
		q.answer(output.suffix(q.getSuffix().length()));
	}

	protected Word<O> getMultipleRunOutput(Word<I> input) {
		TestRunnerResult<I, O> result = TestRunner.runTest(input, runs,
				sulOracle);
		Iterator<Word<O>> outputIter = result.getGeneratedOutputs().keySet()
				.iterator();
		Word<O> checkedOutput = null;
		if (result.getGeneratedOutputs().size() > 1) {
			log.println("Non determinism when running test multple times");
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
		return checkedOutput;
	}

	private Word<O> getProbabilisticOutput(Word<I> input) {
		log.println("Performing probabilistic sanitization");
		int probabilisticReruns = runs * PROBABILISTIC_MULTIPLIER;
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
					outputIter.next()).makeCompact();
		}
	}

}
