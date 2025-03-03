package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.modifiablevariable.bytearray.ByteArrayExplicitValueModification;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.dtls.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.state.State;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.Arrays;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class ClientHelloInput extends DtlsInput {

    @XmlAttribute(name = "suite", required = true)
    private CipherSuite suite;

    /**
     * option needed to learn DTLS implementations which use cookie-less handshake
     * messages
     */
    @XmlAttribute(name = "forceDigest", required = false)
    private boolean forceDigest = false;

    /**
     * option for resetting the digests
     */
    @XmlAttribute(name = "resetDigest", required = false)
    private boolean resetDigest = true;

    /**
     * option for including the latest session id in the client hello
     */
    @XmlAttribute(name = "withSessionId", required = false)
    private boolean withSessionId = false;

    private ClientHelloMessage message;

    public ClientHelloInput() {
        super("CLIENT_HELLO");
    }

    public ClientHelloInput(CipherSuite cipherSuite) {
        super(cipherSuite.name() + "_CLIENT_HELLO");
        this.suite = cipherSuite;
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        getConfig(context).setDefaultClientSupportedCipherSuites(Arrays.asList(suite));
        if (suite.name().contains("EC")) {
            getConfig(context).setAddECPointFormatExtension(true);
            getConfig(context).setAddEllipticCurveExtension(true);
        } else {
            getConfig(context).setAddECPointFormatExtension(false);
            getConfig(context).setAddEllipticCurveExtension(false);
        }

        if(getConfig(context).getHighestProtocolVersion().isDTLS13()) {
            // aka supported_groups
            getConfig(context).setAddEllipticCurveExtension(true);
        }

        if (resetDigest && !getConfig(context).getHighestProtocolVersion().isDTLS13()) {
            getTlsContext(context).getDigest().reset();
        }

        ClientHelloMessage message = new ClientHelloMessage(getConfig(context));

        // we exclude the sessionId
        if (!withSessionId) {
            ModifiableByteArray sbyte = new ModifiableByteArray();
            sbyte.setModification(new ByteArrayExplicitValueModification(new byte[] {}));
            message.setSessionId(sbyte);
        }

        this.message = message;
        return new TlsProtocolMessage(message);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

    public void postSendDtlsUpdate(State state, TlsExecutionContext context) {
        // the second conjunction is used in case TLS-Attacker is updated
        // to work also with 1-CH DTLS handshakes.
        // (in which case, the clienthello is digested)
        if (forceDigest && state.getTlsContext().getDigest().getRawBytes().length == 0) {
            DtlsHandshakeMessageFragment fragment = state.getTlsContext().getDtlsFragmentLayer().wrapInSingleFragment(state.getTlsContext().getContext(), message, true);
            state.getTlsContext().getDigest().append(fragment.getCompleteResultingMessage().getValue());
        }
    }

    public CipherSuite getCipherSuite() {
        return suite;
    }

}
