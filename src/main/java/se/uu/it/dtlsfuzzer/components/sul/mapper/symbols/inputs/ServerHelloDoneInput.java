package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloDoneMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class ServerHelloDoneInput extends DtlsInput {

    public ServerHelloDoneInput() {
        super("SERVER_HELLO_DONE");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        ServerHelloDoneMessage shd = new ServerHelloDoneMessage(getConfig(context));
        return new TlsProtocolMessage(shd);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
