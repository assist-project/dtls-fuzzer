package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.protocol.message.ApplicationMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class ApplicationInput extends DtlsInput {

	@XmlAttribute(name = "suppressAlert", required = false)
	private boolean suppressAlert = false;

	public ApplicationInput() {
		super("APPLICATION_DATA");
	}

	@Override
	public TlsMessage generateMessage(State state, ExecutionContext context) {
		ApplicationMessage appMessage = new ApplicationMessage(state.getConfig(),
				DatatypeConverter.parseHexBinary("5468697320697320612068656c6c6f206d65737361676521"));

		return appMessage;
	}

	@Override
	public TlsOutput postReceiveUpdate(TlsOutput output, State state, ExecutionContext context) {
		if (suppressAlert && output.toString().contains("Alert(FATAL,UNEXPECTED_MESSAGE)")) {
			return TlsOutput.timeout();
		}
		return super.postReceiveUpdate(output, state, context);
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.APPLICATION;
	}

}
