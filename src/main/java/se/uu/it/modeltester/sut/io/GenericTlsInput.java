package se.uu.it.modeltester.sut.io;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import de.rub.nds.modifiablevariable.ModifiableVariable;
import de.rub.nds.tlsattacker.core.https.HttpsRequestMessage;
import de.rub.nds.tlsattacker.core.https.HttpsResponseMessage;
import de.rub.nds.tlsattacker.core.protocol.ModifiableVariableHolder;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ApplicationMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateRequestMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateVerifyMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.DHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.DHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.EncryptedExtensionsMessage;
import de.rub.nds.tlsattacker.core.protocol.message.EndOfEarlyDataMessage;
import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import de.rub.nds.tlsattacker.core.protocol.message.GOSTClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.HeartbeatMessage;
import de.rub.nds.tlsattacker.core.protocol.message.HelloRequestMessage;
import de.rub.nds.tlsattacker.core.protocol.message.HelloRetryRequestMessage;
import de.rub.nds.tlsattacker.core.protocol.message.HelloVerifyRequestMessage;
import de.rub.nds.tlsattacker.core.protocol.message.NewSessionTicketMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskDhClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskDheServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskEcDhClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskEcDheServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskRsaClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SSL2ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SSL2ClientMasterKeyMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SSL2ServerHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SSL2ServerVerifyMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloDoneMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ServerHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SrpClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SrpServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.SupplementalDataMessage;
import de.rub.nds.tlsattacker.core.protocol.message.UnknownHandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.UnknownMessage;
import de.rub.nds.tlsattacker.core.state.State;

/**
 * 
 */
public class GenericTlsInput extends NamedTlsInput {
	@XmlElements(value = {
			@XmlElement(type = ProtocolMessage.class, name = "ProtocolMessage"),
			@XmlElement(type = CertificateMessage.class, name = "Certificate"),
			@XmlElement(type = CertificateVerifyMessage.class, name = "CertificateVerify"),
			@XmlElement(type = CertificateRequestMessage.class, name = "CertificateRequest"),
			@XmlElement(type = ClientHelloMessage.class, name = "ClientHello"),
			@XmlElement(type = HelloVerifyRequestMessage.class, name = "HelloVerifyRequest"),
			@XmlElement(type = DHClientKeyExchangeMessage.class, name = "DHClientKeyExchange"),
			@XmlElement(type = DHEServerKeyExchangeMessage.class, name = "DHEServerKeyExchange"),
			@XmlElement(type = ECDHClientKeyExchangeMessage.class, name = "ECDHClientKeyExchange"),
			@XmlElement(type = ECDHEServerKeyExchangeMessage.class, name = "ECDHEServerKeyExchange"),
			@XmlElement(type = PskClientKeyExchangeMessage.class, name = "PSKClientKeyExchange"),
			@XmlElement(type = FinishedMessage.class, name = "Finished"),
			@XmlElement(type = RSAClientKeyExchangeMessage.class, name = "RSAClientKeyExchange"),
			@XmlElement(type = GOSTClientKeyExchangeMessage.class, name = "GOSTClientKeyExchange"),
			@XmlElement(type = ServerHelloDoneMessage.class, name = "ServerHelloDone"),
			@XmlElement(type = ServerHelloMessage.class, name = "ServerHello"),
			@XmlElement(type = AlertMessage.class, name = "Alert"),
			@XmlElement(type = NewSessionTicketMessage.class, name = "NewSessionTicket"),
			@XmlElement(type = ApplicationMessage.class, name = "Application"),
			@XmlElement(type = ChangeCipherSpecMessage.class, name = "ChangeCipherSpec"),
			@XmlElement(type = SSL2ClientHelloMessage.class, name = "SSL2ClientHello"),
			@XmlElement(type = SSL2ServerHelloMessage.class, name = "SSL2ServerHello"),
			@XmlElement(type = SSL2ClientMasterKeyMessage.class, name = "SSL2ClientMasterKey"),
			@XmlElement(type = SSL2ServerVerifyMessage.class, name = "SSL2ServerVerify"),
			@XmlElement(type = UnknownMessage.class, name = "UnknownMessage"),
			@XmlElement(type = UnknownHandshakeMessage.class, name = "UnknownHandshakeMessage"),
			@XmlElement(type = HelloRequestMessage.class, name = "HelloRequest"),
			@XmlElement(type = HeartbeatMessage.class, name = "Heartbeat"),
			@XmlElement(type = SupplementalDataMessage.class, name = "SupplementalDataMessage"),
			@XmlElement(type = EncryptedExtensionsMessage.class, name = "EncryptedExtensionMessage"),
			@XmlElement(type = HttpsRequestMessage.class, name = "HttpsRequest"),
			@XmlElement(type = HttpsResponseMessage.class, name = "HttpsResponse"),
			@XmlElement(type = PskClientKeyExchangeMessage.class, name = "PskClientKeyExchange"),
			@XmlElement(type = PskDhClientKeyExchangeMessage.class, name = "PskDhClientKeyExchange"),
			@XmlElement(type = PskDheServerKeyExchangeMessage.class, name = "PskDheServerKeyExchange"),
			@XmlElement(type = PskEcDhClientKeyExchangeMessage.class, name = "PskEcDhClientKeyExchange"),
			@XmlElement(type = PskEcDheServerKeyExchangeMessage.class, name = "PskEcDheServerKeyExchange"),
			@XmlElement(type = PskRsaClientKeyExchangeMessage.class, name = "PskRsaClientKeyExchange"),
			@XmlElement(type = PskServerKeyExchangeMessage.class, name = "PskServerKeyExchange"),
			@XmlElement(type = SrpServerKeyExchangeMessage.class, name = "SrpServerKeyExchange"),
			@XmlElement(type = SrpClientKeyExchangeMessage.class, name = "SrpClientKeyExchange"),
			@XmlElement(type = EndOfEarlyDataMessage.class, name = "EndOfEarlyData"),
			@XmlElement(type = EncryptedExtensionsMessage.class, name = "EncryptedExtensions"),
			@XmlElement(type = HelloRetryRequestMessage.class, name = "HelloRetryRequest")})
	private ProtocolMessage message;

	private boolean prepare;

	public GenericTlsInput() {
		super(null);
	}

	public GenericTlsInput(ProtocolMessage message) {
		this(message, true);
	}

	public GenericTlsInput(ProtocolMessage message, boolean prepare) {
		super(message.toCompactString());
		this.message = message;
		this.prepare = prepare;
	}

	public ProtocolMessage generateMessage(State state) {
		stripFields(message);
		// message.getHandler(state.getTlsContext()).prepareMessage(message,
		// prepare);
		return message;
	}

	// this is only useful for deserializing once learning is done
	public void postReceiveUpdate(TlsOutput output, State state) {
		stripFields(message);
	}

	// if the name is not set, we use the compact string of a message as a name
	public String toString() {
		String name = getName();
		if (name == null)
			return message.toCompactString();
		else
			return name;
	}

	/*
	 * Sets the original value of all mvar fields to null.
	 */
	private void stripFields(ProtocolMessage message) {
		List<ModifiableVariableHolder> holders = new LinkedList<>();
		holders.addAll(message.getAllModifiableVariableHolders());
		for (ModifiableVariableHolder holder : holders) {
			List<Field> fields = holder.getAllModifiableVariableFields();
			for (Field f : fields) {
				f.setAccessible(true);

				ModifiableVariable mv = null;
				try {
					mv = (ModifiableVariable) f.get(holder);
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					ex.printStackTrace();
				}
				if (mv != null) {
					if (mv.getModification() != null
							|| mv.isCreateRandomModification()) {
						mv.setOriginalValue(null);
					} else {
						try {
							f.set(holder, null);
						} catch (IllegalArgumentException
								| IllegalAccessException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.fromProtocolMessageType(message
				.getProtocolMessageType());
	}

}
