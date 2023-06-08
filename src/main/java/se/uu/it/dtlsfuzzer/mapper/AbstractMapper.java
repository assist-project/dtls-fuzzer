package se.uu.it.dtlsfuzzer.mapper;

import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.config.MapperConfig;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.OutputMapper;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public abstract class AbstractMapper implements Mapper {
    private static final Logger LOGGER = LogManager.getLogger();
    private OutputMapper outputMapper;

    public AbstractMapper(MapperConfig config) {
        this.outputMapper = new OutputMapper(config);
    }

    public final TlsOutput execute(TlsInput input, State state, ExecutionContext context) {
        LOGGER.debug("Executing input symbol {}", input.name());
        TlsOutput output;
        context.getStepContext().setInput(input);
        if (context.isExecutionEnabled() && input.isEnabled(state, context)) {
            output = doExecute(input, state, context);
        } else {
            output = outputMapper.disabled();
        }
        LOGGER.debug("Produced output symbol {}", output.name());
        return output;
    }

    protected abstract TlsOutput doExecute(TlsInput input, State state, ExecutionContext context);

    /**
     * Template method for executing an input.
     * Takes a message sender as parameter.
     * Mappers need not use this function, but they should always call the update calls on the input in the order suggested.
     */
    protected TlsOutput doExecute(TlsInput input, State state, ExecutionContext context, MessageSender sender) {
        TlsMessage message = input.generateMessage(state, context);
        input.preSendUpdate(state, context);
        sender.sendMessage(message, state, context);
        input.postSendUpdate(state, context);
        TlsOutput output = outputMapper.receiveOutput(state, context);
        input.postReceiveUpdate(output, state, context);
        return output;
    }

    protected OutputMapper getOutputMapper() {
        return outputMapper;
    }

    static interface MessageSender {
        void sendMessage(TlsMessage message, State state, ExecutionContext context);
    }
}
