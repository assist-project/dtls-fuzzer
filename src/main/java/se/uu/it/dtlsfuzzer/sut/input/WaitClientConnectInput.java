package se.uu.it.dtlsfuzzer.sut.input;

import org.apache.commons.lang3.NotImplementedException;

import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

public class WaitClientConnectInput extends TlsInput {
	
	@Override
	public TlsMessage generateMessage(State state, ExecutionContext context) {
		throw new NotImplementedException("Cannot generate message");
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.EMPTY;
	}

}
