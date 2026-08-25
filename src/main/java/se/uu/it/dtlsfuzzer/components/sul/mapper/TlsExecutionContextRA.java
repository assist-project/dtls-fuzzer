package se.uu.it.dtlsfuzzer.components.sul.mapper;

import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContextStepped;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.StepContext;

public class TlsExecutionContextRA
    extends ExecutionContextStepped<
        PSymbolInstance,
        PSymbolInstance,
        TlsState,
        StepContext<PSymbolInstance, PSymbolInstance>
    > {

    public TlsExecutionContextRA(TlsState state) {
        //FIXME: This might need to wrap the mealy context to keep them in sync, but let's test with a plain one first.
        //It depends on whether it is only the mapper that wants to interrogate the stepcontext or if the learner will interface with it in some way.
        super(state);
    }

    @Override
    protected StepContext<PSymbolInstance, PSymbolInstance> buildStepContext() {
        return new StepContext<>(stepContexts.size());
    }
}
