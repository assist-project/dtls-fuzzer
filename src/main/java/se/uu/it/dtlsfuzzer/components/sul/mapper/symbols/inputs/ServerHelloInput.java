package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
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

public class ServerHelloInput extends DtlsInput {

    @XmlAttribute(name = "suite", required = true)
    private CipherSuite suite;

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
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        Config config = getConfig(context);

        config.setDefaultServerSupportedCipherSuites(Arrays.asList(suite));
        config.setDefaultClientSupportedCipherSuites(suite);
        if (suite.name().contains("EC")) {
            config.setAddECPointFormatExtension(true);
            config.setAddEllipticCurveExtension(true);
        } else {
            config.setAddECPointFormatExtension(false);
            getConfig(context).setAddEllipticCurveExtension(false);
        }
        if (suite.isPsk()) {
            config.setAddClientCertificateTypeExtension(false);
            config.setAddServerCertificateTypeExtension(false);
        }

        ServerHelloMessage sh = new ServerHelloMessage(config);
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
    public void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
            ExecutionContext context) {
        TlsExecutionContext ctx = getTlsExecutionContext(context);
        if (shortHs && context.isExecutionEnabled()) {
            Pair<? extends ProtocolMessage<?>, Record> lastChPair = null;
            int lastChStepIndex = -1;
            List<? extends Pair<? extends ProtocolMessage<?>, Record>> msgRecPairs = ctx.getReceivedMessagesAndRecords();
            for (int i=0; i<msgRecPairs.size(); i++) {
                if (msgRecPairs.get(i).getKey() instanceof ClientHelloMessage) {
                    lastChStepIndex = i;
                    lastChPair = msgRecPairs.get(i);
                }
            }

            assert lastChPair != null;

            // we reset the digest and append to it, in reverse order SH, the last CH before the SH and optionally the HR which prompted the CH (if such a HR was sent)
            getTlsContext(ctx).getDigest().reset();

            if (digestHR && ctx.getStepContext(lastChStepIndex).getInput() instanceof HelloRequestInput) {
                byte [] hrBytes = ctx.getStepContext(lastChStepIndex).getReceivedRecords().get(0).getCleanProtocolMessageBytes().getValue();
                getTlsContext(context).getDigest().append(hrBytes);
            }
            byte [] chBytes = lastChPair.getRight().getCleanProtocolMessageBytes().getValue();
            byte [] shBytes = ctx.getStepContext().getSentRecords().get(0).getCleanProtocolMessageBytes().getValue();
            getTlsContext(context).getDigest().append(chBytes);
            getTlsContext(context).getDigest().append(shBytes);
        }
        super.postReceiveUpdate(output, abstractOutputChecker, context);
    }
}
