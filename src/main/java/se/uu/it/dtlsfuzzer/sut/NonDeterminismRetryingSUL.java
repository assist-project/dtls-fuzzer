package se.uu.it.dtlsfuzzer.sut;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;

/**
 * 
 * @author paul
 *
 * @param <I>
 * @param <O>
 */
public class NonDeterminismRetryingSUL<I, O> implements SUL<I, O> {
	private final SUL<I, O> sul;
	private final ObservationTree<I, O> root = new ObservationTree<I, O>();
	private final List<I> inputs = new ArrayList<>();
	private final List<O> outputs = new ArrayList<>();
	private int retries;
	private PrintWriter log;

	// the number of confirmation retries should be high
	public NonDeterminismRetryingSUL(SUL<I, O> sul, int confirmationRetries,
			Writer log) {
		this.sul = sul;
		this.retries = confirmationRetries;
		this.log = new PrintWriter(log);
	}

	@Override
	public void post() {
		sul.post();
		inputs.clear();
		outputs.clear();
	}

	@Override
	public void pre() {
		sul.pre();
	}

	@Override
	public O step(I input) throws SULException {
		O result = sul.step(input);
		inputs.add(input);
		List<O> outputs = new ArrayList<O>(this.outputs);
		outputs.add(result);
		try {
			// check for non-determinism: crashes if outputs are inconsistent
			// with previous ones
			root.addObservation(inputs, outputs);
		} catch (CacheInconsistencyException exc) {
			log.println(exc.toString());
			log.println("Retrying inputs " + retries
					+ " times to confirm non-determinism");
			// ok, non determinism detected, could it be a blip? Does it occur
			// at least once
			// if we rerun the sequence $retries times?
			result = retryInputs(inputs, retries);
			log.println("Non-determinism could not be confirmed. Learning can continue");
		}
		this.outputs.add(result);
		return result;
	}

	private O retryInputs(List<I> inputs, int numTimes) throws SULException {
		// we intersperse more breaks in the execution
		pause(1000);
		O result = null;
		for (int i = 0; i < numTimes; i++) {
			List<O> outputs = new ArrayList<O>();
			sul.post();
			sul.pre();
			// we intersperse more breaks in the execution
			pause(1000);
			for (I input : inputs) {
				outputs.add(sul.step(input));
			}
			result = outputs.get(outputs.size() - 1);
			root.addObservation(inputs, outputs);
		}
		return result;
	}

	private void pause(long millis) throws SULException {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new SULException(e);
		}
	}
}
