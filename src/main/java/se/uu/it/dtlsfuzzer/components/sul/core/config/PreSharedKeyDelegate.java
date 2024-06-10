package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.converters.ByteArrayConverter;
import de.rub.nds.tlsattacker.core.config.delegate.Delegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import java.nio.charset.Charset;

/**
 * Implements arguments for configuring PreSharedKeys (key and identity).
 * PreSharedKeys can also be configured via the TLS-Attacker config file (set via -sulConfig).
 */
public class PreSharedKeyDelegate extends Delegate {
    @Parameter(names = "-psk", description = "PreSharedKey to use in hex", converter = ByteArrayConverter.class)
    private byte [] psk;

    @Parameter(names = "-identity", description = "Identity to use")
    private String identity;

    @Override
    public void applyDelegate(Config config) throws ConfigurationException {
        if (psk != null) {
            config.setDefaultPSKKey(psk);
        }
        if (identity != null) {
            config.setDefaultPSKIdentity(identity.getBytes(Charset.forName("UTF-8")));
        }
    }
}
