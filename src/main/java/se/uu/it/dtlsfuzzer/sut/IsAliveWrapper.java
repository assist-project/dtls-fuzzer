package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.OutputMapper;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class IsAliveWrapper implements SUL<TlsInput, TlsOutput> {

    private SUL<TlsInput, TlsOutput> sut;
    private boolean isAlive;
    private OutputMapper outputMapper;

    public IsAliveWrapper(SUL<TlsInput, TlsOutput> sut, OutputMapper outputMapper) {
        this.sut = sut;
        this.outputMapper = outputMapper;
    }

    @Override
    public void pre() {
        sut.pre();
        isAlive = true;
    }

    @Override
    public void post() {
        sut.post();
    }

    @Override
    public TlsOutput step(TlsInput in) throws SULException {
        if (isAlive) {
            TlsOutput out = sut.step(in);
            isAlive = out.isAlive();
//			TODO Uncomment this and update the learned/regression models
//			if (!isAlive) {
//				out = outputMapper.coalesceOutputs(out, outputMapper.socketClosed());
//			}
            return out;
        } else {
            return outputMapper.socketClosed();
        }
    }
}
