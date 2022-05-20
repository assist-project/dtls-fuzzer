package se.uu.it.dtlsfuzzer.learner;

/**
 * Used to store a snapshot of relevant statistics at selected phases during the learning process. 
 */
public class StatisticsSnapshot {

    private long tests;
    private long inputs;
    private long time;

    public StatisticsSnapshot(long tests, long inputs, long time) {
        super();
        this.tests = tests;
        this.inputs = inputs;
        this.time = time;
    }

    public long getTests() {
        return tests;
    }

    public long getInputs() {
        return inputs;
    }

    public long getTime() {
        return time;
    }
}
