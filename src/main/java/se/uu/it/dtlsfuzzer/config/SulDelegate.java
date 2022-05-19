package se.uu.it.dtlsfuzzer.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.beust.jcommander.Parameter;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import se.uu.it.dtlsfuzzer.sut.ProcessLaunchTrigger;

public abstract class SulDelegate {
	public static final String SUL_CONFIG = "/sul.config";
	public static final String FUZZER_DIR = "fuzzer.dir";
	public static final String SUTS_DIR = "suts.dir";

	@Parameter(names = "-protocol", required = false, description = "Protocol analyzed, determines transport layer used", converter = ProtocolVersionConverter.class)
	private ProtocolVersion protocolVersion = ProtocolVersion.DTLS12;

	@Parameter(names = "-timeout", required = false, description = "Time the SUL spends waiting for a response")
	private Integer timeout = 100;

	@Parameter(names = "-inputResponseTimeout", required = false, description = "Time the SUL spends waiting for a response to a particular input. Expected format is: \"input1:value1,input2:value2...\" ", converter = InputResponseTimeoutConverter.class)
	private InputResponseTimeoutMap inputResponseTimeout;

	@Parameter(names = "-rstWait", required = false, description = "Time the SUL waits after executing each query")
	private Long resetWait = 0L;

	@Parameter(names = { "-command", "-cmd" }, required = false, description = "Command for starting the (D)TLS process")
	private String command = null;

	@Parameter(names = { "-terminateCommand",
			"-termCmd" }, required = false, description = "Command for terminating the (D)TLS process. If specified, it is used instead of java.lang.Process#destroy()")
	private String terminateCommand = null;

	@Parameter(names = { "-processDir" }, required = false, description = "The directory of the (D)TLS process")
	private String processDir = null;

	@Parameter(names = { "-redirectOutputStreams",
			"-ros" }, required = false, description = "Redirects (D)TLS process output streams to STDOUT and STDERR.")
	private boolean redirectOutputStreams;

	@Parameter(names = { "-processTrigger" }, required = false, description = "When is the process launched")
	private ProcessLaunchTrigger processTrigger = ProcessLaunchTrigger.NEW_TEST;

	@Parameter(names = "-runWait", required = false, description = "Time waited after running each TLS command")
	private Long runWait = 0L;

	// In case a launch server is used to execute the SUT (as is the case of JSSE
	// and Scandium)
	@Parameter(names = "-resetPort", required = false, description = "Port to which to send a reset command")
	private Integer resetPort = null;

	@Parameter(names = "-resetAddress", required = false, description = "Address to which to send a reset command")
	private String resetAddress = "localhost";

	@Parameter(names = "-resetCommandWait", required = false, description = "Time waited after sending a reset command")
	private Long resetCommandWait = 0L;

	@Parameter(names = "-resetAck", required = false, description = "Wait from acknowledgement from the other side")
	private boolean resetAck = false;

	@Parameter(names = "-sulConfig", required = false, description = "Configuration for the SUL")
	private String sulConfig = null;

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

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public InputResponseTimeoutMap getInputResponseTimeout() {
		return inputResponseTimeout;
	}

	public Long getResetWait() {
		return resetWait;
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

	public Long getRunWait() {
		return runWait;
	}

	public void setRunWait(Long runWait) {
		this.runWait = runWait;
	}

	public Integer getResetPort() {
		return resetPort;
	}

	public String getResetAddress() {
		return resetAddress;
	}

	public Long getResetCommandWait() {
		return resetCommandWait;
	}

	public boolean isResetAck() {
		return resetAck;
	}

	public boolean isRedirectOutputStreams() {
		return redirectOutputStreams;
	}
}
