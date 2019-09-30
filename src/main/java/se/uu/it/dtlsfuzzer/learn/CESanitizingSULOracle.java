package se.uu.it.dtlsfuzzer.learn;

import java.io.Writer;
import java.util.Collection;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.sut.MultipleRunsSULOracle;
import se.uu.it.dtlsfuzzer.sut.NonDeterminismException;
import se.uu.it.dtlsfuzzer.sut.ObservationTree;

/**
 * Adding CE sanitizing at the SULOracle level allows us to avoid having to
 * restart the whole testing process due to spurious counterexamples. It does
 * come at the cost of doing the output comparison twice, but that cost is
 * insignificant in the context of learning DTLS.
 */
public class CESanitizingSULOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, Word<O>>, I, O>
		extends
			MultipleRunsSULOracle<I, O>
		implements
			MealyMembershipOracle<I, O> {

	private static final Logger LOGGER = LogManager
			.getLogger(CESanitizingSULOracle.class);

	// x times cereruns
	private Supplier<A> automatonProvider;

	private boolean skipNonDetTests;

	private ObservationTree<I, O> cache;

	public CESanitizingSULOracle(int ceReruns,
			MealyMembershipOracle<I, O> sulOracle,
			Supplier<A> automatonProvider, ObservationTree<I, O> cache,
			boolean probabilisticSanitization, boolean skipNonDetTests,
			Writer log) {
		super(ceReruns, sulOracle, probabilisticSanitization, log);
		this.automatonProvider = automatonProvider;
		this.skipNonDetTests = skipNonDetTests;
		this.cache = cache;
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
		// no counterexample, meaning it is safe to return the original output
		else {
			returnedOutput = originalOutput;
		}

		// ok, we have what appears to be a counterexample, still we check it
		// against the cache
		if (!returnedOutput.equals(autOutput)) {
			Word<O> outputFromCache = cache.answerQuery(q.getInput(), true);
			if (!outputFromCache.equals(returnedOutput.prefix(outputFromCache
					.length()))) {
				log.println("Output inconsistent with cache, discarding it and returning automaton output");
				log.println("Input: "
						+ q.getInput().prefix(outputFromCache.length()));
				log.println("Spurious output: " + returnedOutput);
				log.println("Cached output: " + outputFromCache);
				log.flush();
				returnedOutput = autOutput;
			}
		}

		q.answer(returnedOutput.suffix(q.getSuffix().length()));
	}

	private Word<O> getCheckedOutput(Word<I> input, Word<O> originalOutput,
			Word<O> autOutput) {
		try {
			Word<O> checkedOutput = super.getMultipleRunOutput(input);

			if (!checkedOutput.equals(originalOutput)) {
				log.println("Output changed following CE verification");
				log.println("Input: " + input);
				log.println("Original output: " + originalOutput);
				log.println("New output: " + checkedOutput);
				if (checkedOutput.equals(autOutput)) {
					log.println("New CE status: not a CE");
				} else {
					log.println("New CE status: is a CE");
				}
				log.flush();
			}
			return checkedOutput;
		} catch (NonDeterminismException exc) {
			if (skipNonDetTests) {
				log.println("NonDetermism in running input");
				log.println(exc);
				log.println("Skipping: " + input);
				log.flush();
				return autOutput;
			} else {
				throw exc;
			}
		}
	}
}
