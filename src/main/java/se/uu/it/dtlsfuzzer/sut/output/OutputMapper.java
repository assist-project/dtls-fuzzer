package se.uu.it.dtlsfuzzer.sut.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DLSequence;

import de.rub.nds.tlsattacker.core.constants.CertificateType;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.UnknownMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.GenericReceiveAction;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;
import se.uu.it.dtlsfuzzer.config.MapperConfig;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;


/**
 * The output mapper performs the following functions:
 * <ol>
 * 	<li> receives the SUT's response (records) over the wire; </li>
 *  <li> processes the response by: </li>
 *  <ul>
 *  	<li> updating the internal state; </li>
 *  	<li> converting response to a corresponding {@link TlsOutput}. </li>
 *  </ul>
 * </ol>
 * 
 * Everything to do with how a response is converted into a TlsOutput should be implemented here.
 * Also implemented are operations over the mapper such as coalescing to outputs into one or splitting an output into its atoms.
 */
public class OutputMapper {
	/*
	 * The minimum number of alert/unknown messages before decryption failure is established.
	 */
	private static final int MIN_ALERTS_IN_DECRYPTION_FAILURE = 2;
	
	/*
	 * The minimum number of times an output has to be generated for the repeating output to be used.
	 * Note that 2 is the only value currently supported.
	 */
	private static final int MIN_REPEATS_FOR_REPEATING_OUTPUT = 2;
	
	private MapperConfig config;
	
	
	public OutputMapper(MapperConfig config) {
		this.config = config;
	}
	
	public TlsOutput receiveOutput(State state, ExecutionContext context) {
		try {
			if (state.getTlsContext().getTransportHandler().isClosed()) {
				return socketClosed();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return socketClosed();
		}
		try {
			GenericReceiveAction action = new GenericReceiveAction(context.getSulDelegate().getRole());
			action.execute(state);
			context.getStepContext().setReceivedMessages(
					action.getMessages());
			context.getStepContext().setReceivedRecords(action.getRecords());
			context.getStepContext().pairReceivedMessagesWithRecords();
			return extractOutput(state, action);
		} catch (Exception ex) {
			ex.printStackTrace();
			return socketClosed();
		}	
	}
	
	public TlsOutput timeout() {
		return TlsOutput.timeout();
	}
	
	public TlsOutput socketClosed() {
		if (config.isSocketClosedAsTimeout()) {
			return TlsOutput.timeout();
		} else {
			return TlsOutput.socketClosed();
		}
	}
	
	public TlsOutput disabled() {
		if (config.isDisabledAsTimeout()) {
			return TlsOutput.timeout();
		} else {
			return TlsOutput.disabled();
		}
	}
	
	private boolean isResponseUnknown(GenericReceiveAction action) {
		if (action.getReceivedMessages().size() >= MIN_ALERTS_IN_DECRYPTION_FAILURE) {
			return action.getReceivedMessages().stream()
					.allMatch(m -> m instanceof AlertMessage || m instanceof UnknownMessage);
		}
		return false;
	}

	/*
	 * Failure to decrypt shows up as a longer sequence of alarm messages.
	 */
	private int unknownResponseLookahed(int currentIndex,
			List<ProtocolMessage> messages) {
		int nextIndex = currentIndex;
		
		ProtocolMessage message = messages.get(nextIndex);
		while ( (message instanceof AlertMessage || message instanceof UnknownMessage) && nextIndex < messages.size()) {
			message = messages.get(nextIndex);
			nextIndex++;
		}
		if (nextIndex - currentIndex >= MIN_ALERTS_IN_DECRYPTION_FAILURE)
			return nextIndex;
		return -1;
	}

	private TlsOutput extractOutput(State state, GenericReceiveAction action) {
		if (isResponseUnknown(action)) {
			return TlsOutput.unknown();
		}
		if (action.getReceivedMessages().isEmpty()) {
			return TlsOutput.timeout();
		} else {
			List<String> abstractMessageStrings = extractAbstractMessageStrings(action.getMessages(), state);
			String abstractOutput = toAbstractOutputString(abstractMessageStrings);
			
			return new TlsOutput(abstractOutput, action.getReceivedMessages());
		}
	}
	
	private List<String> extractAbstractMessageStrings(List<ProtocolMessage> receivedMessages, State state) {
		List<String> outputStrings = new ArrayList<>(receivedMessages.size());
		for (int i = 0; i < receivedMessages.size(); i++) {
			// checking for cases of decryption failures, which which case
			// we add an unknown message
			int nextIndex = unknownResponseLookahed(i, receivedMessages);
			if (nextIndex > 0) {
				outputStrings.add(TlsOutput.unknown().name());
				i = nextIndex;
				if (i == receivedMessages.size()) {
					break;
				}
			}

			ProtocolMessage m = receivedMessages.get(i);
			String outputString = toOutputString(m, state);
			outputStrings.add(outputString);
		}
		return outputStrings;
	}
	
	private String toAbstractOutputString(List<String> abstractMessageStrings) {
		// in case we find repeated occurrences of types of messages, we
		// coalesce them under +, since some implementations may repeat/retransmit the same message an arbitrary number of times.
		StringBuilder builder = new StringBuilder();
		String lastSeen = null;
		boolean skipStar = false;
		
		for (String abstractMessageString : abstractMessageStrings) {
			if (lastSeen != null && lastSeen.equals(abstractMessageString) && config.isMergeRepeating()) {
				if (!skipStar) {
					// insert before ,
					builder.insert(builder.length() - 1, TlsOutput.REPEATING_INDICATOR);
					skipStar = true;
				}
			} else {
				lastSeen = abstractMessageString;
				skipStar = false;
				builder.append(lastSeen);
				builder.append(TlsOutput.MESSAGE_SEPARATOR);
			}
		}
		
		String abstractOutput = builder.substring(0, builder.length() - 1);
		
		// TODO this is a hack to get learning PionDTLS server to work even when retransmissions are allowed
		// PionDTLS may generate one or several HVR outputs.
		// Here we map HVR to HVR+ so that it still appears deterministic.
		if (config.getRepeatingOutputs() != null) {
			for (String outputString : config.getRepeatingOutputs()) {
				abstractOutput = abstractOutput.replaceAll(
						outputString + "\\+?", outputString + "\\+");
			}
		}
		
		return abstractOutput;
	}
	
	public TlsOutput coalesceOutputs(TlsOutput output1, TlsOutput output2) {
		if (output1.isDisabled() || output2.isDisabled() || output1.isSocketClosed() || output2.isSocketClosed())  {
			throw new RuntimeException("Cannot coalesce " + TlsOutput.DISABLED +" or " 
		+ TlsOutput.SOCKET_CLOSED + " outputs");
		}
		if (output1.isTimeout()) {
			return output2;
		}
		if (output2.isTimeout()) {
			return output1;
		}
		String abstraction;
		List<ProtocolMessage> messages = null;
		assert (output1.isRecordResponse() && output2.isRecordResponse()); 
		List<String> absOutputStrings = new LinkedList<>(output1.getAtomicAbstractionStrings(2));
		absOutputStrings.addAll(output2.getAtomicAbstractionStrings(2));
		abstraction = toAbstractOutputString(absOutputStrings);
		if (output1.hasMessages() && output2.hasMessages()) {
			messages = new LinkedList<>(output1.getMessages());
			messages.addAll(output2.getMessages());
		}
		return new TlsOutput(abstraction, messages);
	}
	
	public List<TlsOutput> getAtomicOutputs(TlsOutput output) {
		int minRepeats = config.isMergeRepeating() ? MIN_REPEATS_FOR_REPEATING_OUTPUT : Integer.MAX_VALUE;
		List<TlsOutput> outputs = output.getAtomicOutputs(minRepeats);
		return outputs;
	}
	
	private String toOutputString(ProtocolMessage message, State state) {
		if (message instanceof CertificateMessage) {
			CertificateMessage cert = (CertificateMessage) message;
			if ( cert.getCertificatesListLength().getValue() > 0) {
				String certTypeString = getCertificateTypeString(cert, state);
				return certTypeString + "_" + message.toCompactString();
			} else {
				return "EMPTY_" + message.toCompactString();
			}
		} 
		return message.toCompactString();
	}
	
	/*
	 * Best-effort method to get the certificate type string from a non-empty certificate
	 */
	private String getCertificateTypeString(CertificateMessage message, State state) {
		String certType = "UNKOWN";
		if (message.getCertificateKeyPair() == null) {
			throw new NotImplementedException("Raw public keys not supported");
		} else {
			certType =  message.getCertificateKeyPair().getCertPublicKeyType().name();	
		}
		return certType;
	}	
}
