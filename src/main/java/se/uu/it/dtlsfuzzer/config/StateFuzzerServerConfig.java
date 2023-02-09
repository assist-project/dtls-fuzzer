package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

@Parameters(commandDescription = "Performs state-fuzzing on a DTLS server generating a model of the system")
public class StateFuzzerServerConfig extends StateFuzzerConfig {

	@ParametersDelegate
	private SulClientDelegate sulClientDelegate;

	public StateFuzzerServerConfig() {
		super();
		sulClientDelegate = new SulClientDelegate();
	}

	public StateFuzzerServerConfig(SulClientDelegate sulClientDelegate) {
		this.sulClientDelegate = sulClientDelegate;
	}

	@Override
	public SulDelegate getSulDelegate() {
		return sulClientDelegate;
	}

	public boolean isClient() {
		return false;
	}

    @Override
    public ToolName getToolName() {
        return ToolName.STATE_FUZZER_SERVER;
    }
}
