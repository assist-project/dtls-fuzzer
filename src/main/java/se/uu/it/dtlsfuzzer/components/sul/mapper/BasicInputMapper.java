package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.InputMapper;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.SendAction;
import java.io.IOException;

public class BasicInputMapper extends InputMapper {
    public BasicInputMapper(MapperConfig mapperConfig, AbstractOutputChecker outputChecker) {
        super(mapperConfig, outputChecker);
    }

    @Override
    public void sendMessage(ProtocolMessage message, ExecutionContext context) {
        State state = ((TlsExecutionContext) context).getState().getState();
        TlsMessage tlsMessage = ((TlsProtocolMessage) message).getMessage();
        SendAction sendAction = new SendAction(tlsMessage);
        sendAction.execute(state);
        if (!sendAction.isExecuted()) {
            try {
                state.getTlsContext().getTransportHandler().closeConnection();
            } catch (IOException E) {
                E.printStackTrace();
            }
        }
    }
}
