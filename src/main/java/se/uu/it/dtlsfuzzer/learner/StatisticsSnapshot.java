package se.uu.it.dtlsfuzzer.learner;

public class StatisticsSnapshot {
	public long getQueries() {
		return queries;
	}

	public long getInputs() {
		return inputs;
	}

	public long getTime() {
		return time;
	}

	private long queries;
	private long inputs;
	private long time;

	public StatisticsSnapshot(long queries, long inputs, long time) {
		super();
		this.queries = queries;
		this.inputs = inputs;
		this.time = time;
	}
}
