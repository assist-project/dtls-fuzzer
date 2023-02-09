package se.uu.it.dtlsfuzzer.sut.output;

import java.util.Arrays;
import java.util.Optional;

import de.rub.nds.tlsattacker.core.constants.CertificateKeyType;
import se.uu.it.dtlsfuzzer.sut.input.KeyExchangeAlgorithm;

/**
 * Provides a interface for analyzing outputs so that how the actual strings are formed is decoupled from the checking code.
 */
public class ModelOutputs {
	private static String APPLICATION="APPLICATION";
	private static String FINISHED="FINISHED";
	private static String ALERT="Alert";
	private static String CLOSE_NOTIFY="Alert(WARNING,CLOSE_NOTIFY)";
	private static String UNEXPECTED_MESSAGE="Alert(FATAL,UNEXPECTED_MESSAGE)";
	private static String CLIENT_HELLO="CLIENT_HELLO";
	private static String SERVER_HELLO="SERVER_HELLO";
	private static String CHANGE_CIPHER_SPEC="CHANGE_CIPHER_SPEC";
	private static String CERTIFICATE="CERTIFICATE";
	private static String EMPTY_CERTIFICATE="EMPTY_CERTIFICATE";
	private static String HELLO_VERIFY_REQUEST="HELLO_VERIFY_REQUEST";
	private static String CLIENT_KEY_EXCHANGE="CLIENT_KEY_EXCHANGE";
	private static String SERVER_KEY_EXCHANGE="SERVER_KEY_EXCHANGE";

	public static boolean hasApplication(TlsOutput output) {
		return output.name().contains(APPLICATION);
	}

	public static TlsOutput getApplicationOutput() {
		return new TlsOutput(APPLICATION);
	}

	public static boolean isApplication(TlsOutput output) {
		return output.name().equals(APPLICATION);
	}

	public static boolean hasCertificate(TlsOutput output) {
		return output.name().contains(CERTIFICATE);
	}

	public static boolean hasNonEmptyCertificate(TlsOutput output) {
		for (TlsOutput atomicOutput : output.getAtomicOutputs()) {
			if (atomicOutput.name().contains(CERTIFICATE) && !atomicOutput.name().equals(EMPTY_CERTIFICATE)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasEmptyCertificate(TlsOutput output) {
		return output.name().contains(EMPTY_CERTIFICATE);
	}

	public static boolean hasServerHello(TlsOutput output) {
		return output.name().contains(SERVER_HELLO);
	}

	public static boolean hasClientHello(TlsOutput output) {
		return output.name().contains(CLIENT_HELLO);
	}

	public static TlsOutput getClientHelloOutput() {
		return new TlsOutput(CLIENT_HELLO);
	}

	public static String getClientHelloString() {
		return CLIENT_HELLO;
	}

	public static boolean hasHelloVerifyRequest(TlsOutput output) {
		return output.name().contains(HELLO_VERIFY_REQUEST);
	}

	public static boolean hasChangeCipherSpec(TlsOutput output) {
		return output.name().contains(CHANGE_CIPHER_SPEC);
	}

	public static boolean hasAlert(TlsOutput output) {
		return output.name().contains(ALERT);
	}

	public static boolean hasFinished(TlsOutput output) {
		return output.name().contains(FINISHED);
	}

	public static boolean hasClientKeyExchange(TlsOutput output) {
		return output.name().contains(CLIENT_KEY_EXCHANGE);
	}

	public static boolean hasServerKeyExchange(TlsOutput output) {
		return output.name().contains(SERVER_KEY_EXCHANGE);
	}

	public static CertificateKeyType getClientCertificateType(TlsOutput output) {
		assert(hasCertificate(output) && output.isAtomic());
		if (hasEmptyCertificate(output)) {
			return CertificateKeyType.NONE;
		} else {
			String kex = getParameter(output, 0);
			if (output.name().contains("RAW_EC")) {
				return CertificateKeyType.ECDSA;
			} else {
				for (CertificateKeyType type : CertificateKeyType.values()) {
					if (type.name().equals(kex)) {
						return type;
					}
				}
			}
		}
		throw new RuntimeException("Could not extract key type from certificate output " + output);
	}

	public static CertificateKeyType getCertificateType(TlsOutput output) {
		Optional<CertificateKeyType> opt = Arrays.stream(CertificateKeyType.values()).filter(ctype -> output.name().contains(ctype.name())).findFirst();
		return opt.orElseGet(() -> null);
	}


	public static KeyExchangeAlgorithm getKeyExchangeAlgorithm(TlsOutput output) {
		if (hasClientKeyExchange(output) || hasServerKeyExchange(output)) {
			String keyExchange = output.name().split("_")[0];
			if (keyExchange.endsWith("DHE")) {
				keyExchange = keyExchange.substring(0, keyExchange.length()-1);
			}
			return KeyExchangeAlgorithm.valueOf(keyExchange);
		}
		throw new RuntimeException("Could not extract key exchange algorithm from output " + output);
	}

	private static String getParameter(TlsOutput output, int idx) {
		String[] outputSplit = output.name().split("_");
		return outputSplit[idx];
	}

}
