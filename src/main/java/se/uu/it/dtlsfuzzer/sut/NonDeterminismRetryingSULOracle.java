package se.uu.it.dtlsfuzzer.sut;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.SULException;
import net.automatalib.words.Word;

public class NonDeterminismRetryingSULOracle<I, O>
		implements
			MealyMembershipOracle<I, O> {

	private CachingSULOracle<I, O> sulOracle;
	private PrintWriter log;
	private int retries;

	public NonDeterminismRetryingSULOracle(CachingSULOracle<I, O> sulOracle,
			int retries, Writer nonDetWriter) {
		this.sulOracle = sulOracle;
		this.log = new PrintWriter(nonDetWriter);
		if (retries <= 0) {
			throw new RuntimeException(
					"There is no point in this oracle if the number of retries is not > 0");
		}
		this.retries = retries;
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		for (Query<I, Word<O>> query : queries) {
			Word<O> output = null;
			try {
				output = sulOracle.answerQuery(query.getInput());
			} catch (CacheInconsistencyException exc) {
				log.println(exc.toString());
				log.println("Retrying inputs " + retries
						+ " times to confirm non-determinism");
				// ok, non determinism detected, could it be a blip? Does it
				// occur
				// at least once
				// if we rerun the sequence $retries times?
				output = retryAnswerQuery(query.getInput(), retries);
				log.println("Non-determinism could not be confirmed. Learning can continue");
				log.flush();
			}
			query.answer(output);
		}
	}

	private Word<O> retryAnswerQuery(Word<I> inputs, int numTimes)
			throws SULException {
		Word<O> output = null;
		// we intersperse more breaks in the execution
		pause(1000);
		for (int i = 0; i < numTimes; i++) {
			output = sulOracle.answerQuery(inputs);
			pause(1000);
		}
		return output;
	}

	private void pause(long millis) throws SULException {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new SULException(e);
		}
	}

}
