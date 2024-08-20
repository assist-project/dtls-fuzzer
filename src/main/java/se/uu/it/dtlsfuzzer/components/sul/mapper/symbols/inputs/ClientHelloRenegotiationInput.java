package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.modifiablevariable.bytearray.ByteArrayExplicitValueModification;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class ClientHelloRenegotiationInput extends TlsInput {

    static enum Enabled {
        OWN_EPOCH_CHANGE, SERVER_EPOCH_CHANGE, ALWAYS, ONCE, ON_SERVER_HELLO
    }

    @XmlAttribute(name = "short", required = false)
    private boolean isShort = false;

    @XmlAttribute(name = "suite", required = false)
    private CipherSuite suite = null;

    @XmlAttribute(name = "enabled", required = false)
    private Enabled enabled = Enabled.ALWAYS;

    @XmlAttribute(name = "withCookie", required = false)
    private boolean withCookie = true;

    @XmlAttribute(name = "resetMSeq", required = false)
    private boolean resetMSeq = true;

    public ClientHelloRenegotiationInput() {
        super("CLIENT_HELLO_RENEGOTIATION");
    }

    @Override
    public boolean isEnabled(ExecutionContext context) {
        switch (enabled) {
            case OWN_EPOCH_CHANGE :
                // send epoch is 1 or more
                return getTlsContext(context).getWriteEpoch() > 0;
            case SERVER_EPOCH_CHANGE :
                // receive epoch is 1 or more
                return getTlsContext(context).getReadEpoch() > 0;
            case ONCE :
                return getTlsExecutionContext(context)
                        .getTlsStepContextStream()
                        .noneMatch(s -> this.equals(s.getInput()) && s.getIndex() != getTlsExecutionContext(context).getStepCount() - 1);
            default :
                return true;
        }
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        getTlsContext(context).getDigest().reset();
        if (resetMSeq) {
            getTlsContext(context).setWriteSequenceNumber(getTlsContext(context).getWriteEpoch(), 0);
        }
        getTlsContext(context).setReadSequenceNumber(getTlsContext(context).getReadEpoch(), 0);
        if (suite != null) {
            getConfig(context).setDefaultClientSupportedCipherSuites(suite);
        }
        ClientHelloMessage message = new ClientHelloMessage(getConfig(context));
        if (!isShort) {
            ModifiableByteArray sbyte = new ModifiableByteArray();
            sbyte.setModification(new ByteArrayExplicitValueModification(
                    new byte[]{}));
            message.setSessionId(sbyte);
        }

        // mbedtls will only engage in renegotiation if the cookie is empty
        if (!withCookie && getTlsContext(context).getDtlsCookie() != null) {
            ModifiableByteArray sbyte = new ModifiableByteArray();
            sbyte.setModification(new ByteArrayExplicitValueModification(
                    new byte[]{}));
            message.setCookie(sbyte);
        }

        return new TlsProtocolMessage(message);
    }

    @Override
    public void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
            ExecutionContext context) {
        switch (enabled) {
            case ON_SERVER_HELLO :
                if (!TlsOutputChecker.hasServerHello(output)) {
                    getTlsExecutionContext(context).disableExecution();
                }
                break;
            default :
                break;
        }
        super.postReceiveUpdate(output, abstractOutputChecker, context);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
