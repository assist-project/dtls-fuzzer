package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import de.rub.nds.x509attacker.constants.X509PublicKeyType;
import java.util.Arrays;
import java.util.Optional;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.KeyExchangeAlgorithm;

/**
 * Provides an interface for analyzing outputs so that how the actual strings are formed is decoupled from the checking code.
 */
public class TlsOutputChecker implements OutputChecker<TlsOutput> {
    private static String APPLICATION = "APPLICATION";
    private static String FINISHED = "FINISHED";
    private static String ALERT = "Alert";
    // private static String CLOSE_NOTIFY="Alert(WARNING,CLOSE_NOTIFY)";
    // private static String UNEXPECTED_MESSAGE="Alert(FATAL,UNEXPECTED_MESSAGE)";
    private static String CLIENT_HELLO = "CLIENT_HELLO";
    private static String SERVER_HELLO = "SERVER_HELLO";
    private static String CHANGE_CIPHER_SPEC = "CHANGE_CIPHER_SPEC";
    private static String CERTIFICATE = "CERTIFICATE";
    private static String EMPTY_CERTIFICATE = "EMPTY_CERTIFICATE";
    private static String HELLO_VERIFY_REQUEST = "HELLO_VERIFY_REQUEST";
    private static String CLIENT_KEY_EXCHANGE = "CLIENT_KEY_EXCHANGE";
    private static String SERVER_KEY_EXCHANGE = "SERVER_KEY_EXCHANGE";

    // TODO: Symbols taken from the PSF OutputBuilder, if we are to use the fixed
    // strings there they should ideally be decoupled further, so they can be
    // present in both the outputchecker and the ourputbuilder without copy/paste
    // One possible solution is an enumeration string type.
    // The above strings might want to be marked final as well.
    /**
     * Special output symbol to show that no response was received during the
     * waiting time.
     */
    static final String TIMEOUT = "TIMEOUT";

    /** Special output symbol to show that the response could not be identified. */
    static final String UNKNOWN = "UNKNOWN";

    /** Special output symbol to show that the SUL process has terminated. */
    static final String SOCKET_CLOSED = "SOCKET_CLOSED";

    /** Special output symbol to show that the output is disabled. */
    static final String DISABLED = "DISABLED";

    public static boolean hasApplication(TlsOutput output) {
        return output.getName().contains(APPLICATION);
    }

    public static TlsOutput getApplicationOutput() {
        return new TlsOutput(APPLICATION);
    }

    public static boolean isApplication(TlsOutput output) {
        return output.getName().equals(APPLICATION);
    }

    public static boolean hasCertificate(TlsOutput output) {
        return output.getName().contains(CERTIFICATE);
    }

    public static boolean hasNonEmptyCertificate(TlsOutput output) {
        for (TlsOutput atomicOutput : output.getAtomicOutputs()) {
            if (atomicOutput.getName().contains(CERTIFICATE) && !atomicOutput.getName().equals(EMPTY_CERTIFICATE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEmptyCertificate(TlsOutput output) {
        return output.getName().contains(EMPTY_CERTIFICATE);
    }

    public static boolean hasServerHello(TlsOutput output) {
        return output.getName().contains(SERVER_HELLO);
    }

    public static boolean hasClientHello(TlsOutput output) {
        return output.getName().contains(CLIENT_HELLO);
    }

    @Override
    public boolean hasInitialClientMessage(TlsOutput abstractOutput) {
        return hasClientHello(abstractOutput);
    }

    public static TlsOutput getClientHelloOutput() {
        return new TlsOutput(CLIENT_HELLO);
    }

    public static String getClientHelloString() {
        return CLIENT_HELLO;
    }

    public static boolean hasHelloVerifyRequest(TlsOutput output) {
        return output.getName().contains(HELLO_VERIFY_REQUEST);
    }

    public static boolean hasChangeCipherSpec(TlsOutput output) {
        return output.getName().contains(CHANGE_CIPHER_SPEC);
    }

    public static boolean hasAlert(TlsOutput output) {
        return output.getName().contains(ALERT);
    }

    public static boolean hasFinished(TlsOutput output) {
        return output.getName().contains(FINISHED);
    }

    public static boolean hasClientKeyExchange(TlsOutput output) {
        return output.getName().contains(CLIENT_KEY_EXCHANGE);
    }

    public static boolean hasServerKeyExchange(TlsOutput output) {
        return output.getName().contains(SERVER_KEY_EXCHANGE);
    }

    public static X509PublicKeyType getClientCertificateType(TlsOutput output) {
        assert(hasCertificate(output) && output.isAtomic());
        if (hasEmptyCertificate(output)) {
            return null;
        } else {
            String kex = getParameter(output, 0);
            if (output.getName().contains("RAW_EC")) {
                return X509PublicKeyType.ECDH_ECDSA;
            } else {
                for (X509PublicKeyType type : X509PublicKeyType.values()) {
                    if (type.name().equals(kex)) {
                        return type;
                    }
                }
            }
        }
        throw new RuntimeException("Could not extract key type from certificate output " + output);
    }

    public static X509PublicKeyType getCertificateType(TlsOutput output) {
        Optional<X509PublicKeyType> opt = Arrays.stream(X509PublicKeyType.values()).filter(ctype -> output.getName().contains(ctype.name())).findFirst();
        return opt.orElseGet(() -> null);
    }


    public static KeyExchangeAlgorithm getKeyExchangeAlgorithm(TlsOutput output) {
        if (hasClientKeyExchange(output) || hasServerKeyExchange(output)) {
            String keyExchange = output.getName().split("_", -1)[0];
            if (keyExchange.endsWith("DHE")) {
                keyExchange = keyExchange.substring(0, keyExchange.length()-1);
            }
            return KeyExchangeAlgorithm.valueOf(keyExchange);
        }
        throw new RuntimeException("Could not extract key exchange algorithm from output " + output);
    }

    private static String getParameter(TlsOutput output, int idx) {
        String[] outputSplit = output.getName().split("_", -1);
        return outputSplit[idx];
    }

    @Override
    public boolean isTimeout(TlsOutput output) {
        return output.getName().contains(TIMEOUT);
    }

    @Override
    public boolean isUnknown(TlsOutput output) {
        return output.getName().contains(UNKNOWN);
    }

    @Override
    public boolean isSocketClosed(TlsOutput output) {
        return output.getName().contains(SOCKET_CLOSED);
    }

    @Override
    public boolean isDisabled(TlsOutput output) {
        return output.getName().contains(DISABLED);
    }
}
