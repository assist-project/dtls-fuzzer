package se.uu.it.modeltester;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;

import de.learnlib.api.SUL;
import de.learnlib.eqtests.basic.WpMethodEQOracle;
import de.learnlib.eqtests.basic.WpMethodEQOracle.MealyWpMethodEQOracle;
import de.learnlib.oracles.CounterOracle;
import de.learnlib.oracles.CounterOracle.MealyCounterOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle;
import de.learnlib.oracles.SimulatorOracle.MealySimulatorOracle;
import de.rub.nds.modifiablevariable.ModifiableVariable;
import de.rub.nds.modifiablevariable.ModificationFilter;
import de.rub.nds.modifiablevariable.VariableModification;
import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.certificate.CertificateByteChooser;
import de.rub.nds.tlsattacker.core.certificate.CertificateKeyPair;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import de.rub.nds.tlsattacker.core.constants.CertificateKeyType;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.crypto.keys.CustomRsaPublicKey;
import de.rub.nds.tlsattacker.core.protocol.message.ApplicationMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateVerifyMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.protocol.message.FinishedMessage;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ExtensionMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.util.UnlimitedStrengthEnabler;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.commons.util.Pair;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ListAlphabet;
import se.uu.it.modeltester.config.ModelBasedTesterConfig;
import se.uu.it.modeltester.execute.TestingInputExecutor;
import se.uu.it.modeltester.learn.RandomWpMethodEQOracle;
import se.uu.it.modeltester.mutate.MutatedTlsInput;
import se.uu.it.modeltester.mutate.MutatingTlsInput;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.Mutator;
import se.uu.it.modeltester.mutate.fragment.FragmentReplayMutation;
import se.uu.it.modeltester.mutate.fragment.FragmentationStrategy;
import se.uu.it.modeltester.mutate.fragment.SplittingMutator;
import se.uu.it.modeltester.mutate.record.RecordDeferMutation;
import se.uu.it.modeltester.mutate.record.RecordDupMutation;
import se.uu.it.modeltester.mutate.record.RecordFlushMutation;
import se.uu.it.modeltester.sut.ProcessHandler;
import se.uu.it.modeltester.sut.ProcessLaunchTrigger;
import se.uu.it.modeltester.sut.SulProcessWrapper;
import se.uu.it.modeltester.sut.TlsSUL;
import se.uu.it.modeltester.sut.io.AlphabetSerializer;
import se.uu.it.modeltester.sut.io.ChangeCipherSpecInput;
import se.uu.it.modeltester.sut.io.ClientHelloInput;
import se.uu.it.modeltester.sut.io.ClientKeyExchangeInput;
import se.uu.it.modeltester.sut.io.FinishedInput;
import se.uu.it.modeltester.sut.io.GenericTlsInput;
import se.uu.it.modeltester.sut.io.KeyExchangeAlgorithm;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;
import se.uu.it.modeltester.sut.io.definitions.Definitions;
import se.uu.it.modeltester.sut.io.definitions.DefinitionsFactory;
import se.uu.it.modeltester.sut.io.definitions.InputDefinition;

// an ugly test harness.
public class Trace {

	private static void init() {
		// essential parts
		Security.addProvider(new BouncyCastleProvider());
		UnlimitedStrengthEnabler.enable();
		// Configurator.setAllLevels("de.rub.nds.tlsattacker", Level.ALL);
	}

	private static List<CipherSuite> DEFAULT_CIPHERS = Arrays
			.asList(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);

	static class Command {
		private static String opensslDtlsRsa = "openssl s_server -key /home/paul/Keys/RSA1024/server-key.pem -cert "
				+ "/home/paul/Keys/RSA1024/server-cert.pem -accept 20000 -dtls1_2 -timeout 5000 -mtu 5000";

		private static String opensslTlsRsa = "openssl s_server -key /home/paul/Keys/RSA1024/server-key.pem -cert "
				+ "/home/paul/Keys/RSA1024/server-cert.pem -accept 20000";

		private static String opensslDtls101dRsa = "/home/paul/Modules/openssl-1.0.1d/apps/openssl s_server "
				+ "-key /home/paul/Keys/RSA1024/server-key.pem -cert /home/paul/Keys/RSA1024/server-cert.pem "
				+ "-accept 20000 -dtls1 -mtu 1500";

		private static String opensslDtls111All = "/home/paul/Modules/openssl-1.1.1c/apps/openssl  s_server -state -psk 1234 -key /home/paul/Keys/RSA1024/server-key.pem -cert /home/paul/Keys/RSA1024/server-cert.pem -CAfile /home/paul/GitHub/TLS-Attacker-Development/TLS-Core/src/main/resources/certs/rsa1024_cert.pem -Verify 1 -accept 20000 -dtls1_2 -timeout 5000 -mtu 5000";

		private static String opensslTls111aRsa = "/home/paul/Modules/openssl-1.1.1a/apps/openssl s_server \"\n"
				+ "				+ \"-key /home/paul/Keys/RSA1024/server-key.pem -cert /home/paul/Keys/RSA1024/server-cert.pem \"\n"
				+ "				+ \"-accept 20000";

		private static String opensslDtlsPsk = "openssl s_server -nocert -psk 1234 -accept 20000 -dtls1_2";

		private static String opensslTlsPsk = "openssl s_server -nocert -psk 1234 -accept 20000";

		private static String gnutlsDtlsRsa = "gnutls-serv --udp --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem --mtu 1500 -p 20000 --disable-client-cert";

		private static String gnutlsTlsRsa = "gnutls-serv --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem -p 20000 --disable-client-cert";

		private static String gnutlsDtlsAll = "gnutls-serv --udp --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem --mtu 1500 -p 20000 "
				+ "--x509cafile /home/paul/GitHub/TLS-Attacker-Development/TLS-Core/src/main/resources/certs/rsa1024_cert.pem "
				+ " --pskpasswd /home/paul/Scripts/gnutls/keys.psk --priority NORMAL:+PSK:+SRP";

		private static String localGnutlsDtlsRsa = "/home/paul/Modules/gnutls-3.5.19/src/gnutls-serv --udp --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem --mtu 1500 -p 20000 --disable-client-cert";

		private static String localGnutlsTlsRsa = "/home/paul/Modules/gnutls-3.5.19/src/gnutls-serv --udp --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem --mtu 1500 -p 20000 --disable-client-cert";

		private static String localGnutlsDtlsPsk = "/home/paul/Modules/gnutls-3.5.19/src/gnutls-serv --udp --disable-client-cert --pskpasswd /home/paul/Scripts/gnutls/keys.psk --port 20000 --priority NORMAL:+PSK:+SRP";

		private static String localGnuTLSNewPsk = "/home/paul/Modules/gnutls-3.6.4/src/gnutls-serv --udp --disable-client-cert --pskpasswd /home/paul/Scripts/gnutls/keys.psk --port 20000 --priority NORMAL:+PSK:+SRP";

		private static String localTinyDtls = "/home/paul/Modules/tinydtls-fuzz/tests/dtls-server -p 20000";

		private static String localETinyDtls = "/home/paul/GitHub/etinydtls/tests/dtls-server -p 20000";

		private static String localMbedDtls = "/home/paul/Modules/mbedtls-2.16.1/programs/ssl/ssl_server2 dtls=1 psk=1234 mtu=5000 key_file=/home/paul/Keys/RSA2048/server-key.pem crt_file=/home/paul/Keys/RSA2048/server-cert.pem server_port=20000 exchanges=100 hs_timeout=20000-120000 ca_file=/home/paul/GitHub/TLS-Attacker-Development/TLS-Core/src/main/resources/certs/rsa2048_cert.pem auth_mode=required debug_level=100";

		private static String none = null;
	}

	static class Handshake {
		public static TlsInput[] handshake(CipherSuite cs,
				@Nullable Boolean inclCert) {
			List<TlsInput> inputs = new ArrayList<>();
			KeyExchangeAlgorithm alg = KeyExchangeAlgorithm
					.getKeyExchangeAlgorithm(cs);
			inputs.add(new ClientHelloInput(cs));
			inputs.add(new ClientHelloInput(cs));
			if (inclCert != null) {
				CertificateMessage cm = new CertificateMessage();
				if (!inclCert) {
					cm.setCertificatesList(Collections.emptyList());
				}
				inputs.add(new GenericTlsInput(cm));
			}
			inputs.add(new ClientKeyExchangeInput(alg));
			if (inclCert != null && inclCert) {
				inputs.add(new GenericTlsInput(new CertificateVerifyMessage()));
			}
			inputs.add(new GenericTlsInput(new ChangeCipherSpecMessage()));
			inputs.add(new FinishedInput());

			return inputs.toArray(new TlsInput[inputs.size()]);
		}
	}

	private static int NUM_FRAGS = 5;

	private static TlsInput fuzz(TlsInput input) {
		SplittingMutator fragmentationMutator = new SplittingMutator(
				FragmentationStrategy.EVEN, NUM_FRAGS);
		return new MutatingTlsInput(input, Arrays.asList(fragmentationMutator));
	}

	public static TlsInput mutating(TlsInput input, Mutator<?>... mutators) {
		return new MutatingTlsInput(input, Arrays.asList(mutators));
	}

	public static TlsInput mutated(TlsInput input, Mutation<?>... mutations) {
		return new MutatedTlsInput(input, Arrays.asList(mutations));
	}

	public static TlsInput nonmut(TlsInput input) {
		return input;
	}

	public static Definitions loadDefinitions(String alphabetFile)
			throws Exception {
		Alphabet<TlsInput> alpha = AlphabetSerializer.read(new FileInputStream(
				alphabetFile));
		System.out.println("Alphabet: " + Arrays.asList(alpha.toArray()));
		Definitions def = DefinitionsFactory.generateDefinitions(alpha);
		return def;
	}

	public static TlsInput[] buildTest(String testString, Definitions def) {
		List<TlsInput> inputs = new ArrayList<>();
		for (String s : testString.split("\\s")) {
			TlsInput input = def.getInputWithDefinition(s.trim());
			// input.setExecutor(new NonMutatingInputExecutor());
			inputs.add(input);
		}
		return inputs.toArray(new TlsInput[inputs.size()]);
	}

	public static TlsInput[] buildTest(String testString, String alphaFile)
			throws Exception {
		Definitions def = loadDefinitions(alphaFile);
		TlsInput[] inputs = buildTest(testString, def);
		return inputs;
	}

	public static String[] tests = {
			// generates malloc failure on openssl
			"CLIENT_HELLO_RSA CLIENT_HELLO_RSA FINISHED APPLICATION",
			// non-det
			"CLIENT_HELLO_RSA CLIENT_HELLO_RSA FINISHED APPLICATION Alert(WARNING,CLOSE_NOTIFY) Alert(WARNING,CLOSE_NOTIFY) CLIENT_HELLO_RSA APPLICATION CHANGE_CIPHER_SPEC FINISHED Alert(WARNING,CLOSE_NOTIFY) APPLICATION CLIENT_HELLO_RSA Alert(WARNING,CLOSE_NOTIFY) Alert(WARNING,CLOSE_NOTIFY) CHANGE_CIPHER_SPEC APPLICATION CLIENT_HELLO_RSA",
			// Alert(UNDEFINED,CLOSE_NOTIFY) Alert(UNDEFINED,CLOSE_NOTIFY)
			// CLIENT_HELLO_RSA APPLICATION CHANGE_CIPHER_SPEC FINISHED
			// Alert(UNDEFINED,CLOSE_NOTIFY) APPLICATION CLIENT_HELLO_RSA
			// Alert(UNDEFINED,CLOSE_NOTIFY) Alert(UNDEFINED,CLOSE_NOTIFY)
			// CHANGE_CIPHER_SPEC APPLICATION CLIENT_HELLO_RSA
			"FINISHED Alert(WARNING,CLOSE_NOTIFY) Alert(WARNING,CLOSE_NOTIFY) CHANGE_CIPHER_SPEC FINISHED Alert(WARNING,CLOSE_NOTIFY) APPLICATION FINISHED Alert(WARNING,CLOSE_NOTIFY) FINISHED Alert(WARNING,CLOSE_NOTIFY) CHANGE_CIPHER_SPEC CLIENT_HELLO_RSA FINISHED Alert(WARNING,CLOSE_NOTIFY) Alert(WARNING,CLOSE_NOTIFY) APPLICATION CLIENT_HELLO_RSA CLIENT_HELLO_RSA Alert(WARNING,CLOSE_NOTIFY) Alert(WARNING,CLOSE_NOTIFY) Alert(WARNING,CLOSE_NOTIFY) FINISHED Alert(WARNING,CLOSE_NOTIFY) APPLICATION CLIENT_HELLO_RSA",
			"CLIENT_HELLO_RSA Alert(WARNING,CLOSE_NOTIFY)",
			// openssl non-det
			"RSA_CLIENT_HELLO RSA_CLIENT_HELLO RSA_CLIENT_KEY_EXCHANGE CHANGE_CIPHER_SPEC FINISHED CHANGE_CIPHER_SPEC Alert(WARNING,CLOSE_NOTIFY) FINISHED APPLICATION Alert(FATAL,UNEXPECTED_MESSAGE) FINISHED RSA_CLIENT_HELLO RSA_CLIENT_KEY_EXCHANGE Alert(WARNING,CLOSE_NOTIFY) FINISHED FINISHED RSA_CLIENT_KEY_EXCHANGE FINISHED Alert(FATAL,UNEXPECTED_MESSAGE) Alert(FATAL,UNEXPECTED_MESSAGE) CHANGE_CIPHER_SPEC RSA_CLIENT_HELLO FINISHED Alert(FATAL,UNEXPECTED_MESSAGE) FINISHED Alert(WARNING,CLOSE_NOTIFY) Alert(FATAL,UNEXPECTED_MESSAGE) Alert(FATAL,UNEXPECTED_MESSAGE) RSA_CLIENT_KEY_EXCHANGE APPLICATION RSA_CLIENT_KEY_EXCHANGE Alert(FATAL,UNEXPECTED_MESSAGE) FINISHED CHANGE_CIPHER_SPEC RSA_CLIENT_KEY_EXCHANGE Alert(FATAL,UNEXPECTED_MESSAGE) RSA_CLIENT_KEY_EXCHANGE Alert(FATAL,UNEXPECTED_MESSAGE) APPLICATION RSA_CLIENT_KEY_EXCHANGE CHANGE_CIPHER_SPEC Alert(FATAL,UNEXPECTED_MESSAGE) FINISHED CHANGE_CIPHER_SPEC CHANGE_CIPHER_SPEC Alert(WARNING,CLOSE_NOTIFY) FINISHED Alert(WARNING,CLOSE_NOTIFY) Alert(FATAL,UNEXPECTED_MESSAGE) Alert(WARNING,CLOSE_NOTIFY) RSA_CLIENT_HELLO RSA_CLIENT_KEY_EXCHANGE RSA_CLIENT_KEY_EXCHANGE RSA_CLIENT_HELLO RSA_CLIENT_KEY_EXCHANGE CHANGE_CIPHER_SPEC RSA_CLIENT_HELLO"};

	// test exposing tinydtls bug
	public static TlsInput[] tinyDtlsOooFinished = new TlsInput[]{
			nonmut(new ClientHelloInput(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8)),
			nonmut(new ClientHelloInput(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8)),
			mutated(new GenericTlsInput(new PskClientKeyExchangeMessage())),
			mutated(new ChangeCipherSpecInput(), new RecordDeferMutation()),
			mutated(new FinishedInput(), new RecordFlushMutation(),
					new RecordDupMutation(-2))};

	// test exposing tinydtls bug
	public static TlsInput[] tinyDtlsOooFinishedBeforeKex = new TlsInput[]{
			nonmut(new ClientHelloInput(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8)),
			nonmut(new ClientHelloInput(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8)),
			mutated(new GenericTlsInput(new PskClientKeyExchangeMessage()),
					new RecordDeferMutation()),
			mutated(new ChangeCipherSpecInput(), new RecordDeferMutation()),
			mutated(new FinishedInput(), new RecordFlushMutation(),
					new RecordDupMutation(-2))};

	public static TlsInput[] regularDtlsOooFinished = new TlsInput[]{
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA)),
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA)),
			mutated(new GenericTlsInput(new PskClientKeyExchangeMessage())),
			mutated(new ChangeCipherSpecInput(), new RecordDeferMutation()),
			mutated(new FinishedInput(), new RecordFlushMutation(),
					new RecordDupMutation(-2))};

	public static TlsInput[] regularOooFinishedBeforeKex = new TlsInput[]{
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA)),
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA)),
			mutated(new GenericTlsInput(new PskClientKeyExchangeMessage()),
					new RecordDeferMutation()),
			mutated(new ChangeCipherSpecInput(), new RecordDeferMutation()),
			mutated(new FinishedInput(), new RecordFlushMutation(),
					new RecordDupMutation(-3))};

	public static TlsInput[] opensslKexSwitch1 = new TlsInput[]{
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			// new GenericTlsInput(new PskClientKeyExchangeMessage()),
			nonmut(new GenericTlsInput(new CertificateMessage())),
			nonmut(new ClientKeyExchangeInput(KeyExchangeAlgorithm.PSK) {
				public ProtocolMessage generateMessage(State state) {
					ProtocolMessage msg = super.generateMessage(state);
					HandshakeMessage hMsg = (HandshakeMessage) msg;
					hMsg.setIncludeInDigest(true);
					return hMsg;
				}

			}

			),
			nonmut(new GenericTlsInput(new CertificateVerifyMessage())),
			nonmut(new ClientKeyExchangeInput(KeyExchangeAlgorithm.RSA) {
				public ProtocolMessage generateMessage(State state) {
					state.getTlsContext().setDtlsNextSendSequenceNumber(
							state.getTlsContext()
									.getDtlsNextSendSequenceNumber() - 2);
					return super.generateMessage(state);
				}

				public void postReceiveUpdate(TlsOutput output, State state) {
					// state.getTlsContext().setDtlsNextSendSequenceNumber(state.getTlsContext().getDtlsNextSendSequenceNumber()+2);
				}

			}), nonmut(new GenericTlsInput(new CertificateVerifyMessage())),
			nonmut(new GenericTlsInput(new ChangeCipherSpecMessage())),
	// nonmut(new GenericTlsInput(new FinishedMessage()))
	};

	public static TlsInput[] opensslBadKex = new TlsInput[]{
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			// new GenericTlsInput(new PskClientKeyExchangeMessage()),
			nonmut(new GenericTlsInput(new CertificateMessage())),
			nonmut(new ClientKeyExchangeInput(KeyExchangeAlgorithm.PSK)),
			nonmut(new GenericTlsInput(new CertificateVerifyMessage())),
			nonmut(new GenericTlsInput(new ChangeCipherSpecMessage())),
	// nonmut(new GenericTlsInput(new FinishedMessage()))
	};

	public static TlsInput[] tinyDtlsNextEpochBug = new TlsInput[]{
			// nonmut(new GenericTlsInput(new ChangeCipherSpecMessage())),
			nonmut(new ClientHelloInput(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8) {
				public ProtocolMessage generateMessage(State state) {
					state.getTlsContext().setDtlsSendEpoch(1);
					return super.generateMessage(state);
				}
			}),
			nonmut(new ClientHelloInput(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8) {
				public void postReceiveUpdate(TlsOutput output, State state) {
					state.getTlsContext().setDtlsSendEpoch(0);
				}
			}),
			// new GenericTlsInput(new PskClientKeyExchangeMessage()),
			nonmut(new ClientKeyExchangeInput(KeyExchangeAlgorithm.PSK)),
			nonmut(new GenericTlsInput(new ChangeCipherSpecMessage())),
			nonmut(new FinishedInput()),};

	public static TlsInput[] wolfsslApplication = new TlsInput[]{
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			// new GenericTlsInput(new PskClientKeyExchangeMessage()),
			nonmut(new GenericTlsInput(new CertificateMessage())),
			nonmut(new ClientKeyExchangeInput(KeyExchangeAlgorithm.RSA)),
			nonmut(new GenericTlsInput(new CertificateVerifyMessage())),
			nonmut(new GenericTlsInput(new ChangeCipherSpecMessage())),
			nonmut(new FinishedInput()),
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			nonmut(new GenericTlsInput(new ChangeCipherSpecMessage())),
			nonmut(new FinishedInput()),};

	// nonmut(new ClientHelloInput(
	// CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
	// mutated(new FinishedInput(), new RecordDeferMutation()),
	// mutated(new GenericTlsInput(new ApplicationMessage()), new
	// RecordFlushMutation()

	@XmlRootElement
	static class CertificateHolder {
		@XmlElement(name = "certificatePair")
		private CertificateKeyPair pair;

		public CertificateHolder() {
		}

		public CertificateHolder(CertificateKeyPair pair) {
			this.pair = pair;
		}
	}

	public static void kexPlay() throws JAXBException {

	}

	public static void certPlay() throws JAXBException {
		init();
		Config config = Config.createConfig();
		CertificateByteChooser chooser = CertificateByteChooser.getInstance();
		State state = new State();
		CertificateKeyPair cert = chooser.chooseCertificateKeyPair(state
				.getTlsContext().getChooser());
		TlsContext context = state.getTlsContext();
		CustomRsaPublicKey pkey = (CustomRsaPublicKey) cert.getPublicKey();

		// System.out.println(cert.toString());
		// System.out.println("Modulus: " + pkey.getModulus());
		// System.out.println("Modulus (1024 rshift): " +
		// pkey.getModulus().shiftRight(1020));
		// System.out.println(ArrayConverter.bytesToHexString(pkey.getModulus().toByteArray())
		// );

		context.setSelectedCipherSuite(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256);

		CertificateKeyPair defaultPair = config
				.getDefaultExplicitCertificateKeyPair();
		CustomRsaPublicKey defaultKey = (CustomRsaPublicKey) defaultPair
				.getPublicKey();

		System.out.println(defaultKey.getModulus());
		System.out.println(ArrayConverter.bytesToHexString(defaultKey
				.getModulus().toByteArray()));
		CertificateMessage message = new CertificateMessage();
		message.getHandler(state.getTlsContext()).prepareMessage(message);
		CustomRsaPublicKey certMsgKey = (CustomRsaPublicKey) message
				.getCertificateKeyPair().getPublicKey();
		System.out.println(defaultKey.getModulus());

		CertificateKeyPair rightPair = null;

		for (CertificateKeyPair pair : chooser.getCertificateKeyPairList()) {
			if (pair.getCertPublicKeyType() == CertificateKeyType.RSA) {
				CustomRsaPublicKey key = (CustomRsaPublicKey) pair
						.getPublicKey();
				if (key.getModulus().toByteArray().length == 257) {
					rightPair = pair;
				}
			}
		}

		JAXBContext jContext = JAXBContext.newInstance(CertificateHolder.class,
				CertificateKeyPair.class);
		Marshaller m = jContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter sw = new StringWriter();
		m.marshal(new CertificateHolder(rightPair), sw);
		System.out.println(sw.toString());
	}

	public static void main(String[] args) throws Exception {
		runTest2();
	}

	public static void runTest2() throws Exception {

		//
		CipherSuite rsa = CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA;

		CipherSuite psk = CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA;

		CipherSuite dh = CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256;
		CipherSuite ecdh = CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256;

		int iterations = 1;
		int stepWait = 50;
		long runWait = 100;

		TlsInput[] inputs = Handshake.handshake(rsa, true);

		runTest(Command.none, wolfsslApplication, iterations, stepWait, runWait);
	}

	public static void runTest() throws Exception {

		//
		CipherSuite cs =
		// CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8;
		// CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA;
		CipherSuite.TLS_PSK_WITH_AES_128_CCM_8;
		int iterations = 1;
		int stepWait = 50;
		long runWait = 100;

		TlsInput[] inputs = new TlsInput[]{
				nonmut(new ClientHelloInput(cs)),
				nonmut(new ClientHelloInput(cs)),
				// new GenericTlsInput(new PskClientKeyExchangeMessage()),
				nonmut(new GenericTlsInput(new PskClientKeyExchangeMessage())),
				mutated(new ChangeCipherSpecInput(), new RecordDeferMutation()),
				mutated(new FinishedInput(), new RecordFlushMutation(),
						new RecordDupMutation(-2))};

		runTest(Command.none, inputs, iterations, stepWait, runWait);
	}

	private static void compareStateMaps() throws Exception {
		FastMealy<TlsInput, TlsOutput> mealy = parseMealy(
				Paths.get("gnutls.dot"),
				Paths.get("examples", "alphabets", "psk_rsa_cert.xml"));
		// parseMealy(Paths.get("experiments", "models",
		// "mbedtls_psk_rsa_cert_nreq_20190510.dot"), Paths.get("examples",
		// "alphabets", "psk_rsa_cert.xml"));
		FastMealyState<TlsOutput> state = mealy.getStates().stream().findAny()
				.get();
		Map<String, Word<TlsInput>> map1 = stateMap(mealy,
				mealy.getInputAlphabet());
		System.out.println(map1);
		Map<String, Word<TlsInput>> map2 = stateMap(mealy,
				mealy.getInputAlphabet());
		System.out.println(map2);
		ArrayList<TlsInput> al = new ArrayList<>(mealy.getInputAlphabet());
		Collections.shuffle(al);
		Map<String, Word<TlsInput>> map3 = stateMap(mealy,
				new ListAlphabet<TlsInput>(al));
	}

	private static Map<String, Word<TlsInput>> stateMap(
			FastMealy<TlsInput, TlsOutput> mealy, Alphabet<TlsInput> a) {
		TreeMap<String, Word<TlsInput>> map = new TreeMap<>();
		Automata.stateCover(mealy, a).forEach(
				w -> map.put(mealy.getState(w).toString(), w));
		return map;
	}

	private static void runTest(String command, TlsInput[] inputs,
			int iterations, Integer stepWait, Long runWait)
			throws InterruptedException {
		SUL<TlsInput, TlsOutput> sut = setupSut(command, stepWait, runWait);

		Map<List<TlsOutput>, Integer> allResponses = new LinkedHashMap<>();
		for (int i = 0; i < iterations; i++) {
			TlsOutput[] responses = new TlsOutput[inputs.length];
			int count = 0;
			sut.pre();
			for (TlsInput input : inputs) {
				responses[count++] = sut.step(input);
			}
			sut.post();
			List<TlsOutput> resList = Arrays.asList(responses);
			if (!allResponses.containsKey(resList)) {
				allResponses.put(resList, 1);
			} else {
				allResponses.put(resList, allResponses.get(resList) + 1);
			}
		}

		System.out.println("Test: " + Arrays.asList(inputs));
		for (Entry<List<TlsOutput>, Integer> res : allResponses.entrySet()) {
			System.out.println(res.getValue() + " times:" + res.getKey());
		}

		for (Entry<List<TlsOutput>, Integer> res : allResponses.entrySet()) {
			System.out.println(res.getValue() + " times:"
					+ compact(res.getKey()));
		}

		Supplier<Object> a;
	}

	private static void runTest(String command, TlsInput[] inputs,
			int iterations, Integer stepWait, Long runWait,
			Map<Integer, Consumer<TlsContext>> preInputUpdate)
			throws InterruptedException {
		SUL<TlsInput, TlsOutput> sut = setupSut(command, stepWait, runWait);

		Map<List<TlsOutput>, Integer> allResponses = new LinkedHashMap<>();
		for (int i = 0; i < iterations; i++) {
			TlsOutput[] responses = new TlsOutput[inputs.length];
			int count = 0;
			sut.pre();
			for (TlsInput input : inputs) {
				responses[count++] = sut.step(input);
			}
			sut.post();
			List<TlsOutput> resList = Arrays.asList(responses);
			if (!allResponses.containsKey(resList)) {
				allResponses.put(resList, 1);
			} else {
				allResponses.put(resList, allResponses.get(resList) + 1);
			}
		}

		System.out.println("Test: " + Arrays.asList(inputs));
		for (Entry<List<TlsOutput>, Integer> res : allResponses.entrySet()) {
			System.out.println(res.getValue() + " times:" + res.getKey());
		}

		for (Entry<List<TlsOutput>, Integer> res : allResponses.entrySet()) {
			System.out.println(res.getValue() + " times:"
					+ compact(res.getKey()));
		}
	}

	private static SUL<TlsInput, TlsOutput> setupSut(String command,
			Integer stepWait, Long runWait) {
		init();
		// "openssl s_server -nocert -psk 1234 -accept 20000 -dtls1_2 -debug";
		// //Command.openssl101dRsa;

		ModelBasedTesterConfig modelFuzzConfig = new ModelBasedTesterConfig(
				new GeneralDelegate());
		modelFuzzConfig.getSulDelegate().setHost("localhost:20000");
		modelFuzzConfig.getSulDelegate().setProtocolVersion(
				ProtocolVersion.DTLS12);
		modelFuzzConfig.getSulDelegate().setTimeout(stepWait);
		modelFuzzConfig.getSulDelegate().setRunWait(runWait);
		modelFuzzConfig.getSulDelegate().setCommand(command);

		// vulnerabilityConfig.getClientDelegate().setHost("192.168.56.101:20001");
		SUL<TlsInput, TlsOutput> sut = new TlsSUL(
				modelFuzzConfig.getSulDelegate(), new TestingInputExecutor());
		if (command != Command.none) {
			sut = new SulProcessWrapper<TlsInput, TlsOutput>(sut,
					modelFuzzConfig.getSulDelegate());
		}
		return sut;
	}

	private static long testSuiteSize(Path automatonFile, Path alphabetFile,
			int depth) throws Exception {
		FastMealy<TlsInput, TlsOutput> fastMealy = parseMealy(automatonFile,
				alphabetFile);
		MealySimulatorOracle<TlsInput, TlsOutput> sim = new SimulatorOracle.MealySimulatorOracle<>(
				fastMealy);
		MealyCounterOracle<TlsInput, TlsOutput> counterOracle = new CounterOracle.MealyCounterOracle<>(
				sim, "counter");
		MealyWpMethodEQOracle<TlsInput, TlsOutput> oracle = new WpMethodEQOracle.MealyWpMethodEQOracle<>(
				depth, counterOracle);
		oracle.findCounterExample(fastMealy, fastMealy.getInputAlphabet());
		return counterOracle.getStatisticalData().getCount();
	}

	private static FastMealy<TlsInput, TlsOutput> parseMealy(
			Path automatonFile, Path alphabetFile)
			throws FileNotFoundException, JAXBException, IOException,
			XMLStreamException, ParseException {
		Alphabet<TlsInput> alphabet = AlphabetSerializer
				.read(new FileInputStream(alphabetFile.toString()));
		MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<TlsInput, TlsOutput>(
				new TlsProcessor(
						DefinitionsFactory.generateDefinitions(alphabet)));
		FastMealy<TlsInput, TlsOutput> fastMealy = dotParser.parseAutomaton(
				automatonFile.toString()).get(0);
		return fastMealy;
	}

	private static String compact(List<TlsOutput> reses) {
		return reses.stream().map(r -> compact(r)).collect(Collectors.toList())
				.toString();
	}

	private static String compact(TlsOutput res) {
		return res.toString();
	}

}
