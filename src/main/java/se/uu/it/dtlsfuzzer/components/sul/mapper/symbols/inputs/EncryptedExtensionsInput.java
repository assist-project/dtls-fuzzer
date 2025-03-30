package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.protocol.message.EncryptedExtensionsMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class EncryptedExtensionsInput extends DtlsInput {

    public EncryptedExtensionsInput() {
        super();
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        getConfig(context).setAddEllipticCurveExtension(true);
        EncryptedExtensionsMessage eeMessage = new EncryptedExtensionsMessage(getConfig(context));

        return new TlsProtocolMessage(eeMessage);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
