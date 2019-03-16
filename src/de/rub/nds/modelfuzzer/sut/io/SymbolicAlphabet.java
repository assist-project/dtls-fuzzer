package de.rub.nds.modelfuzzer.sut.io;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.rub.nds.tlsattacker.core.constants.AlertDescription;
import de.rub.nds.tlsattacker.core.constants.AlertLevel;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ApplicationMessage;
import de.rub.nds.tlsattacker.core.protocol.message.HeartbeatMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskClientKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.RSAClientKeyExchangeMessage;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;

/**
 * Defines symbols and their associated word implementations. This makes it easy for the user to specify 
 * a custom input alphabet.
 * 
 */
//TODO it would be nice to be able to specify the cipher suites in the alphabet. There are ways of making this
// more extensible, the question is which is the best.
public class SymbolicAlphabet {
	
	private static final Map<TlsSymbol, TlsInput> symbolicMap;
	static {
		symbolicMap = new EnumMap<TlsSymbol, TlsInput>(TlsSymbol.class);
		symbolicMap.put(TlsSymbol.RSA_CLIENT_HELLO, new ClientHelloInput(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA));
		symbolicMap.put(TlsSymbol.PSK_CLIENT_HELLO, new ClientHelloInput(CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA ));
//		symbolicMap.put(TlsSymbol.RSA_CLIENT_HELLO, new ClientHelloInput(CipherSuite.TLS_RSA_WITH_AES_128_CCM_8));
//		symbolicMap.put(TlsSymbol.PSK_CLIENT_HELLO, new ClientHelloInput(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8));

		symbolicMap.put(TlsSymbol.RSA_CLIENT_KEY_EXCHANGE, new GenericTlsInput(new RSAClientKeyExchangeMessage()));
		symbolicMap.put(TlsSymbol.PSK_CLIENT_KEY_EXCHANGE, new GenericTlsInput(new PskClientKeyExchangeMessage()));
		symbolicMap.put(TlsSymbol.HEARTBEAT, new GenericTlsInput(new HeartbeatMessage()));
		symbolicMap.put(TlsSymbol.ALERT_UNEXPECTED_MESSAGE, new GenericTlsInput(buildAlertMessage(AlertLevel.FATAL, AlertDescription.UNEXPECTED_MESSAGE)));
		symbolicMap.put(TlsSymbol.ALERT_CLOSE_NOTIFY, new GenericTlsInput(buildAlertMessage(AlertLevel.WARNING, AlertDescription.CLOSE_NOTIFY)));
		symbolicMap.put(TlsSymbol.CHANGE_CIPHER_SPEC, new ChangeCipherSpecInput());
		symbolicMap.put(TlsSymbol.FINISHED, new FinishedTlsInput());
		symbolicMap.put(TlsSymbol.APPLICATION, new GenericTlsInput(new ApplicationMessage()));
		symbolicMap.put(TlsSymbol.HELLO_APPLICATION, new GenericTlsInput(buildCustomApplicationMessage("Hello")));
	}
	
	private static AlertMessage buildAlertMessage(AlertLevel level, AlertDescription description) {
		AlertMessage am = new AlertMessage();
		am.setLevel(level.getValue());
		am.setDescription(description.getValue());
		am.setConfig(level, description);
		return am;
	}
	
	private static ApplicationMessage buildCustomApplicationMessage(String text) {
		ApplicationMessage am = new ApplicationMessage();
		am.setData(text.getBytes());
		return am;
	}
	
	
	public static List<TlsSymbol> getAvailableSymbols() {
		return new ArrayList<>(symbolicMap.keySet());
	}
	
	public static TlsInput createWord(TlsSymbol symbol) {
		return symbolicMap.get(symbol);
	}
	
	public static List<TlsInput> createWords(List<TlsSymbol> symbols) {
		List<TlsSymbol> availableSymbols = getAvailableSymbols();
		List<TlsSymbol> unsupportedSymbols = new ArrayList<>(symbols);
		unsupportedSymbols.removeAll(availableSymbols);
		if (!unsupportedSymbols.isEmpty()) {
			throw new UnsupportedOperationException(
					"The following symbols are not supprted: " + unsupportedSymbols + 
					"\n Supported symbols: " + availableSymbols);
		}
		List<TlsInput> TlsInputs = new ArrayList<>(symbols.size());
		for (TlsSymbol string : symbols) {
			TlsInputs.add(symbolicMap.get(string));
		}
		return TlsInputs;
	}
	
	public static Alphabet<TlsInput> createAlphabet(List<TlsSymbol> symbols) {
		List<TlsInput> TlsInputs = createWords(symbols);
		return new ListAlphabet<>(TlsInputs);
	}
}
