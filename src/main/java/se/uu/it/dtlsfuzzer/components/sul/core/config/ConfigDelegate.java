package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.CertificateDelegate;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.core.layer.constant.StackConfiguration;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class ConfigDelegate {
    public static final String CONFIG_PATH = "/sul.config";

    @Parameter(names = "-sulConfig", required = false, description = "TLS-Attacker configuration XML")
    private String sulConfig = null;

    @Parameter(names = "-protocol", required = false, description = "Protocol analyzed, determines transport layer used", converter = ProtocolVersionConverter.class)
    private ProtocolVersion protocolVersion = ProtocolVersion.DTLS12;

    @ParametersDelegate
    private CertificateDelegate certKeyDelegate = new CertificateDelegate();

    @ParametersDelegate
    private PreSharedKeyDelegate preSharedKeyDelegate = new PreSharedKeyDelegate();

    @Parameter(names = "-exportEffectiveSulConfig", required = false, hidden = true, description = "Exports to the specified file the TLS-Attacker configuration file AFTER arguments have been processed.")
    private String exportEffectiveSulConfig;

    public String getSulConfig() {
        return sulConfig;
    }

    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public String getExportEffectiveSulConfig() {
        return exportEffectiveSulConfig;
    }

    public void applyDelegate(Config config) throws ConfigurationException {
        config.setHighestProtocolVersion(getProtocolVersion());
        config.setDefaultSelectedProtocolVersion(getProtocolVersion());
        certKeyDelegate.applyDelegate(config);
        preSharedKeyDelegate.applyDelegate(config);
        if (getProtocolVersion().isDTLS()) {
            config.setDefaultLayerConfiguration(StackConfiguration.DTLS);
        } else {
            throw new ConfigurationException("Only DTLS protocols are supported");
        }
    }

    public InputStream getConfigInputStream() throws IOException {
        if (sulConfig == null) {
            return ConfigDelegate.class.getResource(CONFIG_PATH).openStream();
        } else {
            return new FileInputStream(sulConfig);
        }
    }
}
