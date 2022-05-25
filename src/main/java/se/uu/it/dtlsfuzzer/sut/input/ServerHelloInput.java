package se.uu.it.dtlsfuzzer.sut.input;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.tuple.Pair;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

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
	public TlsMessage generateMessage(State state, ExecutionContext context) {
		state.getConfig().setDefaultServerSupportedCiphersuites(
				Arrays.asList(suite));
		state.getConfig().setDefaultClientSupportedCiphersuites(suite);
		if (suite.name().contains("EC")) {
			state.getConfig().setAddECPointFormatExtension(true);
			state.getConfig().setAddEllipticCurveExtension(true);
		} else {
			state.getConfig().setAddECPointFormatExtension(false);
			state.getConfig().setAddEllipticCurveExtension(false);
		}
		if (suite.isPsk()) {
			state.getConfig().setAddClientCertificateTypeExtension(false);
			state.getConfig().setAddServerCertificateTypeExtension(false);
		}

		ServerHelloMessage message = new ServerHelloMessage(state.getConfig());

		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

	public CipherSuite getCipherSuite() {
		return suite;
	}
	
	@Override
	public TlsOutput postReceiveUpdate(TlsOutput output, State state, ExecutionContext context) {
		if (shortHs && context.isExecutionEnabled()) {
			Pair<TlsMessage, AbstractRecord> lastChPair = null;
			int lastChStepIndex = -1;
			List<Pair<TlsMessage, AbstractRecord>> msgRecPairs = context.getReceivedMessagesAndRecords();
			for (int i=0; i<msgRecPairs.size(); i++) {
				if (msgRecPairs.get(i).getKey() instanceof ClientHelloMessage) {
					lastChStepIndex = i;
					lastChPair = msgRecPairs.get(i);
				}
			}
			
			assert lastChPair != null;
			
			byte [] chBytes = lastChPair.getRight().getCleanProtocolMessageBytes().getValue();
			byte [] shBytes = context.getStepContext().getProcessingUnit().getInitialRecordsToSend().get(0).getCleanProtocolMessageBytes().getValue();
			
			state.getTlsContext().getDigest().reset();
			if (digestHR && context.getStepContext(lastChStepIndex).getInput() instanceof HelloRequestInput) {
				byte [] hrBytes = context.getStepContext(lastChStepIndex).getReceivedRecords().get(0).getCleanProtocolMessageBytes().getValue();
				state.getTlsContext().getDigest().append(hrBytes);
			}
			state.getTlsContext().getDigest().append(chBytes);
			state.getTlsContext().getDigest().append(shBytes);
		}
		return super.postReceiveUpdate(output, state, context);
	}
}
