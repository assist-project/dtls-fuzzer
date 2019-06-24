package se.uu.it.modeltester.config;

import com.beust.jcommander.Parameter;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class SulDelegate extends ClientDelegate {

	@Parameter(names = "-protocol", required = false, description = "Protocol analyzed, determines transport layer used", converter = ProtocolVersionConverter.class)
	private ProtocolVersion protocolVersion = ProtocolVersion.TLS12;

	@Parameter(names = "-timeout", required = false, description = "Time the SUL spends waiting for a response")
	private Integer timeout = 100;

	@Parameter(names = "-rstWait", required = false, description = "Time the SUL waits after executing each query")
	private Long resetWait = 0L;

	@Parameter(names = {"-command", "-cmd"}, required = false, description = "Command for starting the (D)TLS process")
	private String command = null;

	@Parameter(names = {"-processDir"}, required = false, description = "The directory of the (D)TLS process")
	private String processDir = null;

	@Parameter(names = "-runWait", required = false, description = "Time waited after running each TLS command")
	private Long runWait = 0L;

	@Parameter(names = "-sulConfig", required = false, description = "Configuration for the SUL")
	private String sulConfig = null;

	@Parameter(names = "-withApplicationOutput", required = false, description = "Includes the application output in the output generated. Only useful the command was provided")
	private boolean withApplicationOutput = false;

	public SulDelegate() {
		super();
	}

	public void applyDelegate(Config config) throws ConfigurationException {
		super.applyDelegate(config);
		config.getDefaultClientConnection().setTimeout(timeout);
	}

	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocol) {
		this.protocolVersion = protocol;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Long getResetWait() {
		return resetWait;
	}

	public void setResetWait(Long resetWait) {
		this.resetWait = resetWait;
	}

	public Long getRunWait() {
		return runWait;
	}

	public void setRunWait(Long runWait) {
		this.runWait = runWait;
	}

	public String getProcessDir() {
		return processDir;
	}

	public void setProcessDir(String processDir) {
		this.processDir = processDir;
	}

	public String getSulConfig() {
		return sulConfig;
	}

	public void setSulConfig(String sulConfig) {
		this.sulConfig = sulConfig;
	}

	public boolean isWithApplicationOutput() {
		return withApplicationOutput;
	}
}
