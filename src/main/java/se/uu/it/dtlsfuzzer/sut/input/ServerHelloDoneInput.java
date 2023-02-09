package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloDoneMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

public class ServerHelloDoneInput extends DtlsInput {

	public ServerHelloDoneInput() {
		super("SERVER_HELLO_DONE");
	}

	@Override
	public TlsMessage generateMessage(State state, ExecutionContext context) {
		return new ServerHelloDoneMessage(state.getConfig());
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

}
