package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContextStepped;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.StepContext;
import de.learnlib.ralib.words.PSymbolInstance;

public class TlsExecutionContextRA
    extends ExecutionContextStepped<
        PSymbolInstance,
        PSymbolInstance,
        TlsState,
        StepContext<PSymbolInstance, PSymbolInstance>
    > {

    public TlsExecutionContextRA(TlsState state) {
        //FIXME: This might need to wrap the mealy context to keep them in sync, but let's test with a plain one first.
        //It depends on wheter it is only the mapper that wants to interrogate the stepcontext or if the learner will interface with it in some way.
        super(state);
    }

    @Override
    protected StepContext<PSymbolInstance, PSymbolInstance> buildStepContext() {
        return new StepContext<>(stepContexts.size());
    }
}
