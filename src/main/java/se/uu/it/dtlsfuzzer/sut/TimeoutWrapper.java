package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import java.time.Duration;

public class TimeoutWrapper<I, O> implements SUL<I, O> {
    private SUL<I, O> sul;
    private long startTime;
    private Duration duration;

    public TimeoutWrapper(SUL<I, O> sul, Duration duration) {
        this.sul = sul;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }

    @Override
    public void pre() {
        sul.pre();
    }

    @Override
    public void post() {
        sul.post();
        if (System.currentTimeMillis() > duration.toMillis() + startTime) {
            throw new ExperimentTimeoutException(duration);
        }
    }

    @Override
    public O step(I in) throws SULException {
        return sul.step(in);
    }

}
