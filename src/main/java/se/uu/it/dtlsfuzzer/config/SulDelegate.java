package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import se.uu.it.dtlsfuzzer.sut.ProcessLaunchTrigger;

public abstract class SulDelegate {
    public static final String SUL_CONFIG = "/sul.config";
    public static final String FUZZER_DIR = "fuzzer.dir";
    public static final String SUTS_DIR = "suts.dir";

    @Parameter(names = "-protocol", required = false, description = "Protocol analyzed, determines transport layer used", converter = ProtocolVersionConverter.class)
    private ProtocolVersion protocolVersion = ProtocolVersion.DTLS12;

    @Parameter(names = {"-responseWait", "-respWait"}, required = false, description = "Time the SUL spends waiting for a response")
    private Integer responseWait = 100;

    @Parameter(names = "-inputResponseTimeout", required = false, description = "Time the SUL spends waiting for a response to a particular input. Expected format is: \"input1:value1,input2:value2...\" ", converter = InputResponseTimeoutConverter.class)
    private InputResponseTimeoutMap inputResponseTimeout;

    @Parameter(names = {"-command", "-cmd"}, required = false, description = "Command for starting the (D)TLS process")
    private String command = null;

    @Parameter(names = {"-terminateCommand", "-termCmd"}, required = false, description = "Command for terminating the (D)TLS process. If specified, it is used instead of java.lang.Process#destroy()")
    private String terminateCommand = null;

    @Parameter(names = {"-processDir"}, required = false, description = "The directory of the (D)TLS process")
    private String processDir = null;

    @Parameter(names = {"-redirectOutputStreams", "-ros"}, required = false, description = "Redirects (D)TLS process output streams to STDOUT and STDERR.")
    private boolean redirectOutputStreams;

    @Parameter(names = {"-processTrigger"}, required = false, description = "When is the process launched")
    private ProcessLaunchTrigger processTrigger = ProcessLaunchTrigger.NEW_TEST;

    @Parameter(names = "-startWait", required = false, description = "Time waited after executing the command to start the SUT process.")
    private Long startWait = 0L;

    // In case a launch server is used to execute the SUT (as is the case of JSSE and Scandium)
    @ParametersDelegate
    public SulAdapterConfig sulAdapterConfig;

    @Parameter(names = "-sulConfig", required = false, description = "Configuration for the SUL")
    private String sulConfig = null;

    public SulDelegate() {
        sulAdapterConfig = new SulAdapterConfig();
    }

    public abstract String getRole();

    public abstract boolean isClient();

    public abstract void applyDelegate(Config config) throws ConfigurationException;

    public InputStream getSulConfigInputStream() throws IOException {
        if (sulConfig == null) {
            return SulDelegate.class.getResource(SUL_CONFIG).openStream();
        } else {
            return new FileInputStream(sulConfig);
        }
    }

    public Integer getResponseWait() {
        return responseWait;
    }

    public void setResponseWait(Integer timeout) {
        this.responseWait = timeout;
    }

    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public InputResponseTimeoutMap getInputResponseTimeout() {
        return inputResponseTimeout;
    }

    public String getCommand() {
        return command;
    }

    public String getTerminateCommand() {
        return terminateCommand;
    }

    public String getProcessDir() {
        return processDir;
    }

    public ProcessLaunchTrigger getProcessTrigger() {
        return processTrigger;
    }

    public Long getStartWait() {
        return startWait;
    }

    public void setStartWait(Long runWait) {
        this.startWait = runWait;
    }

    public SulAdapterConfig getSulAdapterConfig() {
        return sulAdapterConfig;
    }

    public boolean isRedirectOutputStreams() {
        return redirectOutputStreams;
    }
}
