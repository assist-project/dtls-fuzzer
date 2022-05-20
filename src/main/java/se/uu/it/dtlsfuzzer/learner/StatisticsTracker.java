package se.uu.it.dtlsfuzzer.learner;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.filter.statistic.Counter;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.config.StateFuzzerConfig;

public class StatisticsTracker {

	private Counter inputCounter;
	private Counter testCounter;

	// some tracked statistics
	long learnInputs;
	long learnTests;
	long duration;
	long allInputs;
	long allTests;
	long lastHypInputs;
	long lastHypTests;
	boolean finished;

	// some helper variables
	long lastInputs;
	long lastTests;
	/* Time (ms) relative to the start of the learning experiment */
	long time; 

	// learning inputs and results
	private StateFuzzerConfig config;
	private Alphabet<?> alphabet;
	private List<DefaultQuery<?, ?>> counterexamples;
	private List<HypothesisStatistics> hypStats;
	private StateMachine learnedModel;
	private StateMachine lastHyp;
	private HypothesisStatistics lastHypStats;
	
	// (optional) runtime tracking of the state of the learning process
	static enum State {
		REFINEMENT,
		TESTING,
		FINISHED
	}
	
	private PrintWriter stateWriter;
	private String notFinishedReason;

	/**
	 * Creates a statistics tracker using counters which are updated during the
	 * learning process.
	 * 
	 * @param inputCounter
	 *            counter updated on every input run on the system during both
	 *            learning and testing.
	 * @param testCounter
	 *            counter updated on every test executed on the system during both
	 *            learning and testing.
	 * 
	 */
	public StatisticsTracker(Counter inputCounter, Counter testCounter) {
		this.inputCounter = inputCounter;
		this.testCounter = testCounter;
	}
	
	public void setRuntimeStateTracking(OutputStream stateOutput) {
		stateWriter = new PrintWriter(new OutputStreamWriter(stateOutput));
	}
	
	/*
	 * If runtime state tracking is enabled, prints to stateWriter the new state learning has entered, 
	 * along with state-specific details. 
	 * Should be called only after all data structures (e.g. counterexamples) corresponding to the state have been updated.
	 */
	private void logStateChange(State newState) {
		if (stateWriter != null) {
			stateWriter.printf("(%d) New State: %s %n", System.currentTimeMillis()-time, newState.name());
			stateWriter.flush();
			switch(newState) {
			case FINISHED:
				stateWriter.close();
				stateWriter = null;
				break;
			case REFINEMENT:
				if (!counterexamples.isEmpty()) {
					DefaultQuery<?, ?> lastCe = counterexamples.get(counterexamples.size()-1);
					stateWriter.printf("Refinement CE: %s %n", lastCe.getInput().toString());
					stateWriter.printf("SUT Response: %s %n", lastCe.getOutput().toString());
					// we use raw types to avoid introducing TlsInput dependency in the StatisticsTracker
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Word hypResponse = lastHyp.getMealyMachine().computeOutput( ((Word) lastCe.getInput()));
					stateWriter.printf("HYP Response: %s %n", hypResponse.toString());
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Should be called before starting learning.
	 */
	public void startLearning(StateFuzzerConfig config, Alphabet<?> alphabet) {
		learnInputs = 0;
		learnTests = 0;
		time = System.currentTimeMillis();
		allInputs = 0;
		allTests = 0;
		lastHypInputs = 0;
		lastHypTests = 0;
		this.config = config;
		this.alphabet = alphabet;
		counterexamples = new ArrayList<>();
		finished = false;
		hypStats = new ArrayList<>();
		logStateChange(State.REFINEMENT);
	}

	/**
	 * Should be called every time learning produces a new hypothesis.
	 */
	public void newHypothesis(StateMachine hypothesis) {
		learnInputs += inputCounter.getCount() - lastInputs;
		learnTests += testCounter.getCount() - lastTests;
		lastHypInputs = inputCounter.getCount();
		lastHypTests = testCounter.getCount();
		lastHyp = hypothesis;
		lastHypStats = new HypothesisStatistics();
		lastHypStats.setStates(hypothesis.getMealyMachine().size());
		lastHypStats.setSnapshot(snapshot());
		lastHypStats.setIndex(counterexamples.size());
		hypStats.add(lastHypStats);
		logStateChange(State.TESTING);
	}

	/**
	 * Should be called every time testing (i.e. the EQ Oracle) produces a
	 * counterexample.
	 */
	public void newCounterExample(DefaultQuery<?, ?> counterexample) {
		lastInputs = inputCounter.getCount();
		lastTests = testCounter.getCount();
		counterexamples.add(counterexample);
		lastHypStats.setCounterexample(counterexample);
		lastHypStats.setCounterexampleSnapshot(snapshot());
		logStateChange(State.REFINEMENT);
	}

	/**
	 * Should be called once learning finishes with a learned model or when it
	 * is abruptly terminated yet statistics are still desired. In the latter
	 * case the last hypothesis should be provided.
	 */
	public void finishedLearning(StateMachine learnedModel, boolean finished, String notFinishedReason) {
		this.learnedModel = learnedModel;
		allInputs = inputCounter.getCount();
		allTests = testCounter.getCount();
		duration = System.currentTimeMillis() - time;
		this.finished = finished;
		this.notFinishedReason = notFinishedReason;
		logStateChange(State.FINISHED);
	}

	/**
	 * Should be called after learning finishes and {@link finishedLearning} has
	 * been called.
	 */
	public Statistics generateStatistics() {
		Statistics statistics = new Statistics();
		statistics.setFinished(finished, notFinishedReason);
		statistics.generateRunDescription(config, alphabet);
		statistics.setAllInputs(allInputs);
		statistics.setAllTests(allTests);
		statistics.setLearnInputs(learnInputs);
		statistics.setLearnTests(learnTests);
		statistics.setLastHypInputs(lastHypInputs);
		statistics.setLastHypTests(lastHypTests);
		statistics.setDuration(duration);
		statistics.setCounterexamples(counterexamples);
		statistics.setAlphabetSize(alphabet.size());
		statistics.setStates(learnedModel.getMealyMachine().size());
		statistics.setHypStats(hypStats);
		return statistics;
	}
	
	private StatisticsSnapshot snapshot() {
		return new StatisticsSnapshot(testCounter.getCount(), inputCounter.getCount(), System.currentTimeMillis() - time);
	}
}
