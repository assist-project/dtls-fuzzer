package se.uu.it.dtlsfuzzer.learner;

/**
 * Used to store a snapshot of relevant statistics at selected phases during the learning process. 
 */
public class StatisticsSnapshot {

    private long inputs;
    private long tests;
    private long time;

    public StatisticsSnapshot(long inputs, long tests, long time) {
        super();
        this.inputs = inputs;
        this.tests = tests;
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
