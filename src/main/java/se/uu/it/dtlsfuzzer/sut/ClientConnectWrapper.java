package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.input.WaitClientConnectInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class ClientConnectWrapper  implements SUL<TlsInput, TlsOutput> {
    private boolean first;
    private SUL<TlsInput, TlsOutput> tlsSul;

    public ClientConnectWrapper(SUL<TlsInput, TlsOutput> tlsSul) {
        this.tlsSul = tlsSul;
    }

    @Override
    public void pre() {
        tlsSul.pre();
        first = true;
    }

    @Override
    public void post() {
        tlsSul.post();
    }

    @Override
    public TlsOutput step(TlsInput in) throws SULException {
        TlsOutput output;
        if (in instanceof WaitClientConnectInput && !first
                || !(in instanceof WaitClientConnectInput) && first
                ) {
            output = TlsOutput.disabled();
        } else {
            output = tlsSul.step(in);
        }
        first = false;
        return output;
    }

}
