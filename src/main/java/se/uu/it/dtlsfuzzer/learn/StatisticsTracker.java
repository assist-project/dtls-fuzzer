package se.uu.it.dtlsfuzzer.learn;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.Counter;
import net.automatalib.words.Alphabet;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;

public class StatisticsTracker {

	private Counter inputCounter;
	private Counter resetCounter;

	// some tracked statistics
	long learnInputs;
	long learnResets;
	long time;
	long allInputs;
	long allResets;
	long lastHypInputs;
	long lastHypResets;
	boolean successful;

	// some helper variables
	long lastInputs;
	long lastResets;

	// learning inputs and results
	private DtlsFuzzerConfig config;
	private Alphabet<?> alphabet;
	private List<DefaultQuery<?, ?>> counterexamples;
	private StateMachine learnedModel;

	/**
	 * Creates a statistics tracker using counters which are updated during the
	 * learning process.
	 * 
	 * @param inputCounter
	 *            counter updated on every input run on the system during both
	 *            learning and testing.
	 * @param resetCounter
	 *            counter updated on every reset run on the system during both
	 *            learning and testing.
	 * 
	 */
	public StatisticsTracker(Counter inputCounter, Counter resetCounter) {
		this.inputCounter = inputCounter;
		this.resetCounter = resetCounter;
	}

	/**
	 * Should be called before starting learning.
	 */
	public void startLearning(DtlsFuzzerConfig config, Alphabet<?> alphabet) {
		learnInputs = 0;
		learnResets = 0;
		time = System.currentTimeMillis();
		allInputs = 0;
		allResets = 0;
		lastHypInputs = 0;
		lastHypResets = 0;
		this.config = config;
		this.alphabet = alphabet;
		counterexamples = new ArrayList<>();
		successful = false;
	}

	/**
	 * Should be called every time learning produces a new hypothesis.
	 */
	public void newHypothesis(StateMachine hypothesis) {
		learnInputs += inputCounter.getCount() - lastInputs;
		learnResets += resetCounter.getCount() - lastResets;
		lastHypInputs = inputCounter.getCount();
		lastHypResets = resetCounter.getCount();
	}

	/**
	 * Should be called every time testing (i.e. the EQ Oracle) produces a
	 * counterexample.
	 */
	public void newCounterExample(DefaultQuery<?, ?> counterexample) {
		lastInputs = inputCounter.getCount();
		lastResets = resetCounter.getCount();
		counterexamples.add(counterexample);
	}

	/**
	 * Should be called once learning finishes with a learned model or when it
	 * is abruptly terminated yet statistics are still desired. In the latter
	 * case the last hypothesis should be provided.
	 */
	public void finishedLearning(StateMachine learnedModel, boolean success) {
		this.learnedModel = learnedModel;
		allInputs = inputCounter.getCount();
		allResets = resetCounter.getCount();
		time = System.currentTimeMillis() - time;
		successful = success;
	}

	/**
	 * Should be called after learning finishes and {@link finishedLearning} has
	 * been called.
	 */
	public Statistics generateStatistics() {
		Statistics statistics = new Statistics();
		statistics.setSuccessful(successful);
		statistics.generateRunDescription(config, alphabet);
		statistics.setAllInputs(allInputs);
		statistics.setAllResets(allResets);
		statistics.setLearnInputs(learnInputs);
		statistics.setLearnResets(learnResets);
		statistics.setLastHypInputs(lastHypInputs);
		statistics.setLastHypResets(lastHypResets);
		statistics.setDuration(time);
		statistics.setCounterexamples(counterexamples);
		statistics.setStates(learnedModel.getMealyMachine().size());
		return statistics;
	}
}
