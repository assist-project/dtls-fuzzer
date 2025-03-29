package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.EncryptedExtensionsMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class EncryptedExtensionsInput extends DtlsInput {

    public EncryptedExtensionsInput() {
        super();
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        getConfig(context).setAddEllipticCurveExtension(true);
        EncryptedExtensionsMessage eeMessage = new EncryptedExtensionsMessage(getConfig(context));

        return new TlsProtocolMessage(eeMessage);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
