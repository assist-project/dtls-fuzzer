package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;

public class TestLimitWrapper <I, O> implements SUL<I, O> {

    private SUL<I, O> sul;
    private final long limit;
    private long numTests;

    public TestLimitWrapper(SUL<I,O> sul, long limit) {
        this.sul = sul;
        this.limit = limit;
    }

    @Override
    public void pre() {
        sul.pre();
    }

    @Override
    public void post() {
        sul.post();
        numTests ++;
        if (numTests == limit) {
            throw new TestLimitReachedException(limit);
        }
    }

    @Override
    public O step(I in) {
        return sul.step(in);
    }

}
