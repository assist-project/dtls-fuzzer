package se.uu.it.dtlsfuzzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;

import org.bouncycastle.crypto.tls.Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;

import de.learnlib.api.SUL;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle.MealyCounterOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle.MealyWpMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.oracle.membership.SimulatorOracle.MealySimulatorOracle;
import de.rub.nds.tlsattacker.core.certificate.CertificateKeyPair;
import de.rub.nds.tlsattacker.core.certificate.PemUtil;
import de.rub.nds.tlsattacker.core.constants.AlertDescription;
import de.rub.nds.tlsattacker.core.constants.AlertLevel;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateVerifyMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTraceSerializer;
import de.rub.nds.tlsattacker.core.workflow.action.SendAction;
import de.rub.nds.tlsattacker.util.UnlimitedStrengthEnabler;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.words.Alphabet;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.execute.TestingInputExecutor;
import se.uu.it.dtlsfuzzer.mutate.MutatedTlsInput;
import se.uu.it.dtlsfuzzer.mutate.MutatingTlsInput;
import se.uu.it.dtlsfuzzer.mutate.Mutation;
import se.uu.it.dtlsfuzzer.mutate.Mutator;
import se.uu.it.dtlsfuzzer.mutate.record.RecordDeferMutation;
import se.uu.it.dtlsfuzzer.mutate.record.RecordDupMutation;
import se.uu.it.dtlsfuzzer.mutate.record.RecordFlushMutation;
import se.uu.it.dtlsfuzzer.sut.SulProcessWrapper;
import se.uu.it.dtlsfuzzer.sut.TlsSUL;
import se.uu.it.dtlsfuzzer.sut.io.AlphabetSerializer;
import se.uu.it.dtlsfuzzer.sut.io.ChangeCipherSpecInput;
import se.uu.it.dtlsfuzzer.sut.io.ClientHelloInput;
import se.uu.it.dtlsfuzzer.sut.io.ClientKeyExchangeInput;
import se.uu.it.dtlsfuzzer.sut.io.FinishedInput;
import se.uu.it.dtlsfuzzer.sut.io.GenericTlsInput;
import se.uu.it.dtlsfuzzer.sut.io.KeyExchangeAlgorithm;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;
import se.uu.it.dtlsfuzzer.sut.io.definitions.Definitions;
import se.uu.it.dtlsfuzzer.sut.io.definitions.DefinitionsFactory;

/**
 * This is just an experimental test class which ought to be removed from any
 * production ready code.
 */
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

	public static Definitions loadDefinitions(String alphabetFile)
			throws Exception {
		Alphabet<TlsInput> alpha = AlphabetSerializer.read(new FileInputStream(
				alphabetFile));
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

	public static void testSerialize() throws FileNotFoundException,
			JAXBException, IOException {
		ClientHelloMessage cm = new ClientHelloMessage();
		cm.setCipherSuites(CipherSuite.TLS_AES_128_CCM_8_SHA256.getByteValue());
		WorkflowTrace wt = new WorkflowTrace();
		wt.addTlsAction(new SendAction(cm));
		WorkflowTraceSerializer.write(new File("example"), wt);
	}

	public static String[] tests = {
			// generates malloc failure on openssl
			"CLIENT_HELLO_RSA CLIENT_HELLO_RSA FINISHED APPLICATION",
			// non-det
			"CLIENT_HELLO_RSA CLIENT_HELLO_RSA FINISHED APPLICATION Alert(WARNING,CLOSE_NOTIFY) Alert(WARNING,CLOSE_NOTIFY) CLIENT_HELLO_RSA APPLICATION CHANGE_CIPHER_SPEC FINISHED Alert(WARNING,CLOSE_NOTIFY) APPLICATION CLIENT_HELLO_RSA Alert(WARNING,CLOSE_NOTIFY) Alert(WARNING,CLOSE_NOTIFY) CHANGE_CIPHER_SPEC APPLICATION CLIENT_HELLO_RSA",
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
			nonmut(new GenericTlsInput(new ChangeCipherSpecMessage())),};

	private static AlertMessage getAlert() {
		AlertMessage message = new AlertMessage();
		message.setLevel(AlertLevel.WARNING.getValue());
		message.setDescription(AlertDescription.CLOSE_NOTIFY.getValue());
		return message;
	}

	public static TlsInput[] opensslCookie = new TlsInput[]{
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),
			// new GenericTlsInput(new PskClientKeyExchangeMessage()),
			nonmut(new GenericTlsInput(getAlert()) {
				public void postReceiveUpdate(TlsOutput output, State state) {
					System.out.println(state.getTlsContext().getConnection()
							.getPort());
					state.getTlsContext().setDtlsNextSendSequenceNumber(0);
					state.getTlsContext().setDtlsCookie(null);
				}

			}),
			nonmut(new ClientHelloInput(
					CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA)),

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
				public void postReceiveUpdate(TlsOutput output, State state,
						ExecutionContext context) {
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

	public static void main(String[] args) throws Exception {
		runTest();
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

		TlsInput[] inputs = Handshake.handshake(cs, false);

		runTest(Command.none, inputs, iterations, stepWait, runWait);
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
	}

	/*
	 * Sets up the SUL according to the given params
	 */
	private static SUL<TlsInput, TlsOutput> setupSut(String command,
			Integer stepWait, Long runWait) {
		init();

		DtlsFuzzerConfig modelFuzzConfig = new DtlsFuzzerConfig();
		modelFuzzConfig.getSulDelegate().setHost("localhost:20000");
		modelFuzzConfig.getSulDelegate().setProtocolVersion(
				ProtocolVersion.DTLS12);
		modelFuzzConfig.getSulDelegate().setTimeout(stepWait);
		modelFuzzConfig.getSulDelegate().setRunWait(runWait);
		modelFuzzConfig.getSulDelegate().setCommand(command);

		SUL<TlsInput, TlsOutput> sut = new TlsSUL(
				modelFuzzConfig.getSulDelegate(), new TestingInputExecutor());
		if (command != Command.none) {
			sut = new SulProcessWrapper<TlsInput, TlsOutput>(sut,
					modelFuzzConfig.getSulDelegate());
		}
		return sut;
	}

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

	/*
	 * Utility function used for serializing key pairs.
	 */
	public static void keyPairSerialize(String certPath, String keyPath,
			Writer w) throws JAXBException {
		init();
		CertificateKeyPair keyPair = loadKeyPair(certPath, keyPath);
		JAXBContext jContext = JAXBContext.newInstance(CertificateHolder.class,
				CertificateKeyPair.class);
		Marshaller m = jContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(new CertificateHolder(keyPair), w);
	}

	private static CertificateKeyPair loadKeyPair(String certPath,
			String keyPath) {
		try {
			Certificate readCertificate = PemUtil.readCertificate(new File(
					certPath));
			PrivateKey privateKey = PemUtil.readPrivateKey(new File(keyPath));
			return new CertificateKeyPair(readCertificate, privateKey);
		} catch (Exception E) {
		}
		return null;
	}

	/*
	 * Computes the size of the test suite generated for a given model
	 */
	private static long testSuiteSize(Path automatonFile, Path alphabetFile,
			int depth) throws Exception {
		FastMealy<TlsInput, TlsOutput> fastMealy = parseMealy(automatonFile,
				alphabetFile);
		MealySimulatorOracle<TlsInput, TlsOutput> sim = new SimulatorOracle.MealySimulatorOracle<>(
				fastMealy);
		MealyCounterOracle<TlsInput, TlsOutput> counterOracle = new CounterOracle.MealyCounterOracle<>(
				sim, "counter");
		MealyWpMethodEQOracle<TlsInput, TlsOutput> oracle = new WpMethodEQOracle.MealyWpMethodEQOracle<>(
				counterOracle, depth);
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

	public static TlsInput mutating(TlsInput input, Mutator<?>... mutators) {
		return new MutatingTlsInput(input, Arrays.asList(mutators));
	}

	public static TlsInput mutated(TlsInput input, Mutation<?>... mutations) {
		return new MutatedTlsInput(input, Arrays.asList(mutations));
	}

	public static TlsInput nonmut(TlsInput input) {
		return input;
	}

}
