package de.rub.nds.modelfuzzer.learn;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.oracles.DefaultQuery;
import de.learnlib.statistics.Counter;
import de.rub.nds.modelfuzzer.config.ModelBasedFuzzerConfig;
import net.automatalib.words.Alphabet;

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

	// some helper variables
	long lastInputs;
	long lastResets;

	// learning inputs and results
	private ModelBasedFuzzerConfig config;
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
	public void startLearning(ModelBasedFuzzerConfig config,
			Alphabet<?> alphabet) {
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
	 * Should be called once learning finishes with a learned model.
	 */
	public void finishedLearning(StateMachine learnedModel) {
		this.learnedModel = learnedModel;
		allInputs = inputCounter.getCount();
		allResets = resetCounter.getCount();
		time = System.currentTimeMillis() - time;
	}

	/**
	 * Should be called after learning finishes and {@link finishedLearning} is
	 * called.
	 */
	public Statistics generateStatistics() {
		Statistics statistics = new Statistics();
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
