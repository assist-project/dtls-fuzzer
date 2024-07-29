package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;

import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import de.rub.nds.tlsattacker.core.constants.CertificateKeyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.KeyExchangeAlgorithm;

/**
 * Provides an interface for analyzing outputs so that how the actual strings
 * are formed is decoupled from the checking code.
 */
public class TlsOutputCheckerRA implements OutputChecker<PSymbolInstance> {
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

    public static boolean hasApplication(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(APPLICATION);
    }

    public static PSymbolInstance getApplicationOutput() {
        OutputSymbol baseSymbol = new OutputSymbol(APPLICATION);
        return new PSymbolInstance(baseSymbol);
    }

    public static boolean isApplication(PSymbolInstance output) {
        return output.getBaseSymbol().getName().equals(APPLICATION);
    }

    public static boolean hasCertificate(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(CERTIFICATE);
    }

    public static boolean hasNonEmptyCertificate(PSymbolInstance output) {
        for (PSymbolInstance atomicOutput : getAtomicOutputs(output)) {
            if (atomicOutput.getBaseSymbol().getName().contains(CERTIFICATE)
                    && !atomicOutput.getBaseSymbol().getName().equals(EMPTY_CERTIFICATE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEmptyCertificate(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(EMPTY_CERTIFICATE);
    }

    public static boolean hasServerHello(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(SERVER_HELLO);
    }

    public static boolean hasClientHello(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(CLIENT_HELLO);
    }

    @Override
    public boolean hasInitialClientMessage(PSymbolInstance abstractOutput) {
        return hasClientHello(abstractOutput);
    }

    public static PSymbolInstance getClientHelloOutput() {
        OutputSymbol baseSymbol = new OutputSymbol(CLIENT_HELLO);
        return new PSymbolInstance(baseSymbol);
    }

    public static String getClientHelloString() {
        return CLIENT_HELLO;
    }

    public static boolean hasHelloVerifyRequest(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(HELLO_VERIFY_REQUEST);
    }

    public static boolean hasChangeCipherSpec(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(CHANGE_CIPHER_SPEC);
    }

    public static boolean hasAlert(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(ALERT);
    }

    public static boolean hasFinished(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(FINISHED);
    }

    public static boolean hasClientKeyExchange(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(CLIENT_KEY_EXCHANGE);
    }

    public static boolean hasServerKeyExchange(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(SERVER_KEY_EXCHANGE);
    }

    public static CertificateKeyType getClientCertificateType(PSymbolInstance output) {
        assert (hasCertificate(output) && isAtomic(output));
        if (hasEmptyCertificate(output)) {
            return CertificateKeyType.NONE;
        } else {
            String kex = getParameter(output, 0);
            if (output.getBaseSymbol().getName().contains("RAW_EC")) {
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

    public static CertificateKeyType getCertificateType(PSymbolInstance output) {
        Optional<CertificateKeyType> opt = Arrays.stream(CertificateKeyType.values())
                .filter(ctype -> output.getBaseSymbol().getName().contains(ctype.name())).findFirst();
        return opt.orElseGet(() -> null);
    }

    public static KeyExchangeAlgorithm getKeyExchangeAlgorithm(PSymbolInstance output) {
        if (hasClientKeyExchange(output) || hasServerKeyExchange(output)) {
            String keyExchange = output.getBaseSymbol().getName().split("_", -1)[0];
            if (keyExchange.endsWith("DHE")) {
                keyExchange = keyExchange.substring(0, keyExchange.length() - 1);
            }
            return KeyExchangeAlgorithm.valueOf(keyExchange);
        }
        throw new RuntimeException("Could not extract key exchange algorithm from output " + output);
    }

    private static String getParameter(PSymbolInstance output, int idx) {
        String[] outputSplit = output.getBaseSymbol().getName().split("_", -1);
        return outputSplit[idx];
    }

    @Override
    public boolean isTimeout(PSymbolInstance output) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isTimeout'");
    }

    @Override
    public boolean isUnknown(PSymbolInstance output) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isUnknown'");
    }

    @Override
    public boolean isSocketClosed(PSymbolInstance output) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSocketClosed'");
    }

    @Override
    public boolean isDisabled(PSymbolInstance output) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDisabled'");
    }

    public static boolean isComposite(PSymbolInstance output) {
        return output.getBaseSymbol().getName().contains(MapperOutput.MESSAGE_SEPARATOR);
    }

    public static boolean isAtomic(PSymbolInstance output) {
        return !isComposite(output);
    }

    public static List<PSymbolInstance> getAtomicOutputs(PSymbolInstance output) {
        return getAtomicOutputs(output, 1);
    }

    public static List<PSymbolInstance> getAtomicOutputs(PSymbolInstance output, int unrollRepeating) {
        List<PSymbolInstance> outputs = new ArrayList<>();

        if (isAtomic(output) && !isRepeating(output)) {
            outputs.add(output);
            return outputs;
        }

        for (String absOutput : getAtomicAbstractionStrings(output, unrollRepeating)) {
            ParameterizedSymbol baseSymbol = new OutputSymbol(absOutput);
            PSymbolInstance finalOutput = new PSymbolInstance(baseSymbol);
            outputs.add(finalOutput);
        }
        return outputs;
    }

    public static List<String> getAtomicAbstractionStrings(PSymbolInstance output) {
        return getAtomicAbstractionStrings(output, 1);
    }

    public static List<String> getAtomicAbstractionStrings(PSymbolInstance output, int unrollRepeating) {
        String[] atoms = output.getBaseSymbol().getName().split("\\" + MapperOutput.MESSAGE_SEPARATOR, -1);
        List<String> newAtoms = new ArrayList<>();

        for (String atom : atoms) {
            if (atom.endsWith(MapperOutput.REPEATING_INDICATOR)) {
                String repeatingAtom = atom.substring(0, atom.length() - MapperOutput.REPEATING_INDICATOR.length());
                for (Integer i = 0; i < unrollRepeating; i++) {
                    newAtoms.add(repeatingAtom);
                }
            } else {
                newAtoms.add(atom);
            }
        }

        return newAtoms;
    }

    public static boolean isRepeating(PSymbolInstance output) {
        return isAtomic(output) && output.getBaseSymbol().getName().endsWith(MapperOutput.REPEATING_INDICATOR);
    }

}
