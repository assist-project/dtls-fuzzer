package se.uu.it.dtlsfuzzer.learner;

import de.learnlib.api.query.DefaultQuery;

public class HypothesisStatistics {
	private StatisticsSnapshot snapshot;
	private DefaultQuery<?, ?> counterexample;
	private int states;
	private int index;
	private StatisticsSnapshot counterexampleSnapshot;
	
	
	public int getStates() {
		return states;
	}
	public void setStates(int states) {
		this.states = states;
	}
	public StatisticsSnapshot getCounterexampleSnapshot() {
		return counterexampleSnapshot;
	}
	public void setCounterexampleSnapshot(StatisticsSnapshot counterexampleSnapshot) {
		this.counterexampleSnapshot = counterexampleSnapshot;
	}
	public StatisticsSnapshot getSnapshot() {
		return snapshot;
	}
	public void setSnapshot(StatisticsSnapshot snapshot) {
		this.snapshot = snapshot;
	}
	public DefaultQuery<?, ?> getCounterexample() {
		return counterexample;
	}
	public void setCounterexample(DefaultQuery<?, ?> counterexample) {
		this.counterexample = counterexample;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
}
