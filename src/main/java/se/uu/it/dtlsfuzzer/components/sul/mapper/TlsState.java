package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;

public class TlsState implements com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.State {
    private State state;

    public TlsState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public TlsContext getTlsContext() {
        return state.getTlsContext();
    }
}
