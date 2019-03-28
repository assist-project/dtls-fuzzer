package de.rub.nds.modelfuzzer;


import java.security.Security;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.learnlib.api.SUL;
import de.rub.nds.modelfuzzer.config.ModelBasedFuzzerConfig;
import de.rub.nds.modelfuzzer.fuzz.DtlsMessageFragmenter;
import de.rub.nds.modelfuzzer.fuzz.FragmentationGeneratorFactory;
import de.rub.nds.modelfuzzer.fuzz.FragmentationStrategy;
import de.rub.nds.modelfuzzer.fuzz.FragmentingInputExecutor;
import de.rub.nds.modelfuzzer.sut.ProcessHandler;
import de.rub.nds.modelfuzzer.sut.SulProcessWrapper;
import de.rub.nds.modelfuzzer.sut.TlsSUL;
import de.rub.nds.modelfuzzer.sut.io.ChangeCipherSpecInput;
import de.rub.nds.modelfuzzer.sut.io.ClientHelloInput;
import de.rub.nds.modelfuzzer.sut.io.FinishedInput;
import de.rub.nds.modelfuzzer.sut.io.FuzzedTlsInput;
import de.rub.nds.modelfuzzer.sut.io.GenericTlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.protocol.message.CertificateMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.util.UnlimitedStrengthEnabler;

public class Trace {

	private static void init() {
		// essential parts
		Security.addProvider(new BouncyCastleProvider());
		UnlimitedStrengthEnabler.enable();
		// Configurator.setAllLevels("de.rub.nds.tlsattacker", Level.ALL);
	}

	private static List<CipherSuite> DEFAULT_CIPHERS = Arrays.asList(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);

	

	static class Command {
		private static String opensslDtlsRsa = "openssl s_server -key /home/paul/Keys/RSA1024/server-key.pem -cert "
				+ "/home/paul/Keys/RSA1024/server-cert.pem -accept 20000 -dtls1_2 -timeout 5000 -mtu 5000";
		
		private static String opensslTlsRsa = "openssl s_server -key /home/paul/Keys/RSA1024/server-key.pem -cert "
				+ "/home/paul/Keys/RSA1024/server-cert.pem -accept 20000";
		
		private static String opensslDtls101dRsa = "/home/paul/Modules/openssl-1.0.1d/apps/openssl s_server "
				+ "-key /home/paul/Keys/RSA1024/server-key.pem -cert /home/paul/Keys/RSA1024/server-cert.pem "
				+ "-accept 20000 -dtls1 -mtu 1500";
		
		private static String opensslDtls111aRsa = "/home/paul/Modules/openssl-1.1.1a/apps/openssl s_server "  
				+ "-key /home/paul/Keys/RSA1024/server-key.pem -cert /home/paul/Keys/RSA1024/server-cert.pem "  
				+ "-accept 20000 -dtls1 -mtu 1500";
		
		private static String opensslTls111aRsa = "/home/paul/Modules/openssl-1.1.1a/apps/openssl s_server \"\n" + 
				"				+ \"-key /home/paul/Keys/RSA1024/server-key.pem -cert /home/paul/Keys/RSA1024/server-cert.pem \"\n" + 
				"				+ \"-accept 20000";

		private static String opensslDtlsPsk = "openssl s_server -nocert -psk 1234 -accept 20000 -dtls1_2";
		
		private static String opensslTlsPsk = "openssl s_server -nocert -psk 1234 -accept 20000";
		
		private static String gnutlsDtlsRsa = "gnutls-serv --udp --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem --mtu 1500 -p 20000 --disable-client-cert";
		
		private static String gnutlsTlsRsa = "gnutls-serv --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem -p 20000 --disable-client-cert";

		private static String localGnutlsDtlsRsa = "/home/paul/Modules/gnutls-3.5.19/src/gnutls-serv --udp --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem --mtu 1500 -p 20000 --disable-client-cert";
		
		private static String localGnutlsTlsRsa = "/home/paul/Modules/gnutls-3.5.19/src/gnutls-serv --udp --x509keyfile /home/paul/Keys/RSA2048/server-key.pem "
				+ "--x509certfile /home/paul/Keys/RSA2048/server-cert.pem --mtu 1500 -p 20000 --disable-client-cert";

		private static String localGnutlsDtlsPsk = "/home/paul/Modules/gnutls-3.5.19/src/gnutls-serv --udp --disable-client-cert --pskpasswd /home/paul/Scripts/gnutls/keys.psk --port 20000 --priority NORMAL:+PSK:+SRP";

		private static String localGnuTLSNewPsk = "/home/paul/Modules/gnutls-3.6.4/src/gnutls-serv --udp --disable-client-cert --pskpasswd /home/paul/Scripts/gnutls/keys.psk --port 20000 --priority NORMAL:+PSK:+SRP";

		private static String localTinyDtls = "/home/paul/Modules/tinydtls-fuzz/tests/dtls-server -p 20000";
		
		private static String localMbedDtls = "/home/paul/Modules/mbedtls-2.14.0/programs/ssl/ssl_server2 "
				+ "dtls=1 psk=1234 mtu=100 key_file=/home/paul/Keys/RSA2048/server-key.pem "
				+ "crt_file=/home/paul/Keys/RSA2048/server-cert.pem server_port=20000 exchanges=100 hs_timeout=20000-120000";
		
		private static String none = null;
	}
	
	private static int NUM_FRAGS = 2;
	
	private static TlsInput fuzz(TlsInput input) {
		FragmentingInputExecutor fragmentingExecutor = new FragmentingInputExecutor(
				new DtlsMessageFragmenter(NUM_FRAGS), 
				FragmentationGeneratorFactory.buildGenerator(FragmentationStrategy.EVEN));
		return new FuzzedTlsInput(input, fragmentingExecutor);
	}
	
	private static TlsInput fuzz(TlsInput input, int numFrags) {
		if (numFrags == 0) 
			return input;
		else  {
			FragmentingInputExecutor fragmentingExecutor = new FragmentingInputExecutor(
					new DtlsMessageFragmenter(numFrags), 
					FragmentationGeneratorFactory.buildGenerator(FragmentationStrategy.EVEN));
			return new FuzzedTlsInput(input, fragmentingExecutor);
		}
	}
	
	public static void main(String[] args) throws Exception {

		init();
		CipherSuite cs = 
//				CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256;
//				CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA;
				CipherSuite.TLS_PSK_WITH_AES_128_CCM_8;
		int iterations = 1;
		int stepWait = 200;
		long runWait = 100;
		TlsInput [] inputs = new TlsInput [] {
				fuzz(new ClientHelloInput(cs), 0),
				fuzz(new ClientHelloInput(cs), 0),
				fuzz(new GenericTlsInput(new PskClientKeyExchangeMessage()), 0),
				new ChangeCipherSpecInput(),
				fuzz(new FinishedInput(), 0),
				fuzz(new ClientHelloInput(cs), 1)				
		};
		
		String command = Command.localTinyDtls;
				//"openssl s_server -nocert -psk 1234 -accept 20000 -dtls1_2 -debug"; //Command.openssl101dRsa;
		
		ModelBasedFuzzerConfig modelFuzzConfig = new ModelBasedFuzzerConfig(new GeneralDelegate());
		modelFuzzConfig.getSulDelegate().setHost("localhost:20000");
		modelFuzzConfig.getSulDelegate().setProtocolVersion(ProtocolVersion.DTLS12);
		modelFuzzConfig.getSulDelegate().setTimeout(stepWait);
		modelFuzzConfig.getSulDelegate().setRunWait(runWait);
		modelFuzzConfig.getSulDelegate().setCommand(command);

		// vulnerabilityConfig.getClientDelegate().setHost("192.168.56.101:20001");
		SUL<TlsInput, TlsOutput> sut = new TlsSUL(modelFuzzConfig.getSulDelegate());
		if (command != Command.none) {
			ProcessHandler phandler = new ProcessHandler(modelFuzzConfig.getSulDelegate());
			phandler.redirectOutput(System.err);
			phandler.redirectError(System.err);
			sut = new SulProcessWrapper<TlsInput, TlsOutput>(sut, phandler);
		}

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

		for (Entry<List<TlsOutput>, Integer> res : allResponses.entrySet()) {
			System.out.println(res.getValue() + " times:" + res.getKey());
		}

		for (Entry<List<TlsOutput>, Integer> res : allResponses.entrySet()) {
			System.out.println(res.getValue() + " times:" + compact(res.getKey()));
		}

	}

	private static String compact(List<TlsOutput> reses) {
		return reses.stream().map(r -> compact(r)).collect(Collectors.toList()).toString();
	}

	private static String compact(TlsOutput res) {
		return res.toString();
	}

}
