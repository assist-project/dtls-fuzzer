package se.uu.it.dtlsfuzzer.learner;

public class StatisticsSnapshot {
	public long getResets() {
		return resets;
	}

	public long getInputs() {
		return inputs;
	}

	public long getTime() {
		return time;
	}

	private long resets;
	private long inputs;
	private long time;
	
	public StatisticsSnapshot(long resets, long inputs, long time) {
		super();
		this.resets = resets;
		this.inputs = inputs;
		this.time = time;
	}
}
