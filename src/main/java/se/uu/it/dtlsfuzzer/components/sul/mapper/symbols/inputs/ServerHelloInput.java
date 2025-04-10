package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloMessage;
import de.rub.nds.tlsattacker.core.record.Record;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class ServerHelloInput extends DtlsInput {

    @XmlAttribute(name = "suite", required = true)
    private CipherSuite suite;

    @XmlAttribute(name = "helloRetryRequest", required = false)
    private boolean helloRetryRequest = false;

    @XmlAttribute(name = "shortHs", required = false)
    private boolean shortHs = true;

    @XmlAttribute(name = "digestHR", required = false)
    private boolean digestHR = false;

    public ServerHelloInput() {
        super("SERVER_HELLO");
    }

    public ServerHelloInput(CipherSuite cipherSuite) {
        super(cipherSuite.name() + "_SERVER_HELLO");
        this.suite = cipherSuite;
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        Config config = context.getConfig();

        config.setDefaultServerSupportedCipherSuites(Arrays.asList(suite));
        config.setDefaultClientSupportedCipherSuites(suite);
        if (suite.name().contains("EC")) {
            config.setAddECPointFormatExtension(true);
            config.setAddEllipticCurveExtension(true);
        } else {
            config.setAddECPointFormatExtension(false);
            config.setAddEllipticCurveExtension(false);
        }
        if (suite.isPsk()) {
            config.setAddClientCertificateTypeExtension(false);
            config.setAddServerCertificateTypeExtension(false);
        }
        // record isAddPreSharedKeyExtension(), we need PSK extension when sending ServerHello
        boolean addPSK = config.isAddPreSharedKeyExtension();
        if (helloRetryRequest && config.getHighestProtocolVersion().isDTLS13()) {
            config.setAddPreSharedKeyExtension(false);
            config.setAddKeyShareExtension(false);
            config.setAddCookieExtension(true);
        } else if (config.getHighestProtocolVersion().isDTLS13()) {
            config.setAddKeyShareExtension(true);
            config.setAddCookieExtension(false);
        }

        ServerHelloMessage sh = new ServerHelloMessage(config);
        if (helloRetryRequest && config.getHighestProtocolVersion().isDTLS13()) {
            sh.setRandom(ServerHelloMessage.getHelloRetryRequestRandom());
            // recover isAddPreSharedKeyExtension()
            config.setAddPreSharedKeyExtension(addPSK);
        }
        return new TlsProtocolMessage(sh);
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

    public CipherSuite getCipherSuite() {
        return suite;
    }

    @Override
    public void postReceiveUpdate(TlsOutput output, OutputChecker<TlsOutput> abstractOutputChecker,
            TlsExecutionContext context) {
        if (shortHs && context.isExecutionEnabled()) {
            Pair<ProtocolMessage, Record> lastChPair = null;
            int lastChStepIndex = -1;
            List<Pair<ProtocolMessage, Record>> msgRecPairs = context.getReceivedMessagesAndRecords();
            for (int i = 0; i < msgRecPairs.size(); i++) {
                if (msgRecPairs.get(i).getKey() instanceof ClientHelloMessage) {
                    lastChStepIndex = i;
                    lastChPair = msgRecPairs.get(i);
                }
            }

            assert lastChPair != null;

            // We reset the digest and append to it, in reverse order SH, the last CH before
            // the SH and optionally the HR which prompted the CH (if such a HR was sent).
            context.getTlsContext().getDigest().reset();

            if (digestHR && context.getStepContext(lastChStepIndex).getInput() instanceof HelloRequestInput) {
                byte[] hrBytes = context.getStepContext(lastChStepIndex).getReceivedRecords().get(0).getCleanProtocolMessageBytes().getValue();
                context.getTlsContext().getDigest().append(hrBytes);
            }
            byte[] chBytes = lastChPair.getRight().getCleanProtocolMessageBytes().getValue();
            byte[] shBytes = context.getStepContext().getSentRecords().get(0).getCleanProtocolMessageBytes().getValue();
            context.getTlsContext().getDigest().append(chBytes);
            context.getTlsContext().getDigest().append(shBytes);
        }
        super.postReceiveUpdate(output, abstractOutputChecker, context);
    }
}
