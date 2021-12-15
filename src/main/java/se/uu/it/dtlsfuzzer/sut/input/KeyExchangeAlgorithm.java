package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;

public enum KeyExchangeAlgorithm {

	RSA, PSK, PSK_RSA, DH, ECDH, SRP, GOST;

	/**
	 * Returns the key exchange algorithm corresponding to a cipher suite.
	 */
	public static KeyExchangeAlgorithm getKeyExchangeAlgorithm(CipherSuite cs) {
		if (cs.isPsk()) {
			if (cs.name().contains("RSA"))
				return PSK_RSA;
			return PSK;
		} else {
			if (cs.name().contains("DH")) {
				if (cs.name().contains("ECDH")) {
					return ECDH;
				} else {
					return DH;
				}
			} else {
				if (cs.name().contains("RSA")) {
					return RSA;
				} else {
					if (cs.isSrp()) {
						return SRP;
					} else {
						if (cs.name().contains("GOST")) {
							return GOST;
						} else {
							throw new RuntimeException(
									"Could not find matching key exchange algorithm for cipher suite "
											+ cs.name());
						}
					}
				}
			}
		}
	}

	public boolean isPsk() {
		return this == PSK || this == PSK_RSA;
	}

	public boolean isRsa() {
		return this == RSA;
	}

	public boolean isAnyDH() {
		return this == DH || this == ECDH;
	}
}
