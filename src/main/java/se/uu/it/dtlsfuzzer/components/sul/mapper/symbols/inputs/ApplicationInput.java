package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.ApplicationMessage;
import jakarta.xml.bind.DatatypeConverter;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class ApplicationInput extends DtlsInput {

    public ApplicationInput() {
        super("APPLICATION_DATA");
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        ApplicationMessage appMessage = new ApplicationMessage(DatatypeConverter.parseHexBinary("5468697320697320612068656c6c6f206d65737361676521"));

        return new TlsProtocolMessage(appMessage);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.APPLICATION;
    }

}
