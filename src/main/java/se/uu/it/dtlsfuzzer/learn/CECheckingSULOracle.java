package se.uu.it.dtlsfuzzer.learn;

import java.util.Collection;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.sut.NonDeterminismException;

/**
 * Adding CE checking at the SULOracle level allows us to avoid having to
 * restart the whole testing process due to spurious counterexamples. It does
 * come at the cost of doing the output comparison twice, but that cost is
 * insignificant in the context of learning DTLS.
 */
public class CECheckingSULOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, Word<O>>, I, O>
		implements
			MealyMembershipOracle<I, O> {

	private static final Logger LOGGER = LogManager
			.getLogger(CECheckingSULOracle.class);

	public int ceReruns;

	private MealyMembershipOracle<I, O> sulOracle;
	private Supplier<A> automatonProvider;

	public CECheckingSULOracle(int ceReruns,
			MealyMembershipOracle<I, O> sulOracle, Supplier<A> automatonProvider) {
		this.sulOracle = sulOracle;
		this.automatonProvider = automatonProvider;
		this.ceReruns = ceReruns;
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
			returnedOutput = getCheckedOutput(q.getInput());
		}
		// no counterexample, making it safe to return the original output
		else {
			returnedOutput = originalOutput;
		}

		q.answer(returnedOutput);
	}

	private Word<O> getCheckedOutput(Word<I> input) {
		Word<O> checkedOutput = null, lastOutput = null;
		for (int i = 0; i < ceReruns; i++) {
			checkedOutput = sulOracle.answerQuery(input);
			if (lastOutput != null && !checkedOutput.equals(lastOutput)) {
				throw new NonDeterminismException(input, checkedOutput,
						lastOutput);
			}
			lastOutput = checkedOutput;
		}
		return checkedOutput;
	}

}
