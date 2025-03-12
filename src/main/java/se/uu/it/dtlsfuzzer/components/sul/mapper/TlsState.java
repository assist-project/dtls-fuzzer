package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.rub.nds.tlsattacker.core.layer.context.TlsContext;
import de.rub.nds.tlsattacker.core.state.State;

public class TlsState {
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
