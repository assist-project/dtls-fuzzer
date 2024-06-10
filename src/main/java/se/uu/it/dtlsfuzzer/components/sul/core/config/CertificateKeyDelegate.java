package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import de.rub.nds.tlsattacker.core.certificate.CertificateKeyPair;
import de.rub.nds.tlsattacker.core.certificate.PemUtil;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.Delegate;
import de.rub.nds.tlsattacker.core.crypto.keys.CustomPrivateKey;
import de.rub.nds.tlsattacker.core.util.CertificateUtils;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;
import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import org.bouncycastle.crypto.tls.Certificate;

/**
 * Implements arguments for configuring the certificate/key to use.
 * This can also be configured by changing the TLS-Attacker config file (set via -sulConfig).
 * Code is derived from {@link de.rub.nds.tlsattacker.core.config.delegate.CertificateDelegate}.
 */
@SuppressWarnings("deprecation")
public class CertificateKeyDelegate extends Delegate {
    @Parameter(names = "-cert", description = "PEM encoded certificate file")
    private String certificate;

    @Parameter(names = "-key", description = "PEM encoded private key")
    private String key;

    public CertificateKeyDelegate() {
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void applyDelegate(Config config) {
        PrivateKey privateKey = null;
        if (key != null) {
            LOGGER.debug("Loading private key");
            try {
                privateKey = PemUtil.readPrivateKey(new File(key));
                CustomPrivateKey customPrivateKey = CertificateUtils.parseCustomPrivateKey(privateKey);
                customPrivateKey.adjustInConfig(config, ConnectionEndType.CLIENT);
                customPrivateKey.adjustInConfig(config, ConnectionEndType.SERVER);
            } catch (IOException ex) {
                LOGGER.warn("Could not read private key", ex);
            }
        }
        if (certificate != null) {
            LOGGER.debug("Loading certificate");
            try {
                Certificate cert = PemUtil.readCertificate(new File(certificate));
                if (privateKey != null) {
                    config.setDefaultExplicitCertificateKeyPair(new CertificateKeyPair(cert, privateKey));
                } else {
                    config.setDefaultExplicitCertificateKeyPair(new CertificateKeyPair(cert));
                }
                config.setAutoSelectCertificate(false);
            } catch (Exception ex) {
                LOGGER.warn("Could not read certificate", ex);
            }
        }
    }
}
