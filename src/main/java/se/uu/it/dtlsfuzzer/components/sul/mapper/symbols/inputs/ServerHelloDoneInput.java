package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloDoneMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class ServerHelloDoneInput extends DtlsInput {

    public ServerHelloDoneInput() {
        super("SERVER_HELLO_DONE");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        ServerHelloDoneMessage shd = new ServerHelloDoneMessage();
        return new TlsProtocolMessage(shd);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
