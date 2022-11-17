package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

@Parameters(commandDescription = "Performs state-fuzzing on a DTLS client generating a model of the system")
public class StateFuzzerClientConfig extends StateFuzzerConfig {

	@ParametersDelegate
	private SulServerDelegate sulServerDelegate;
	
	public StateFuzzerClientConfig() {
		super();
		sulServerDelegate = new SulServerDelegate();
	}
	
	public StateFuzzerClientConfig(SulServerDelegate sulServerDelegate) {
		this.sulServerDelegate = sulServerDelegate;
	}
	
	@Override
	public SulDelegate getSulDelegate() {
		return sulServerDelegate;
	}
	
	public boolean isClient() {
		return true;
	}

    @Override
    public ToolName getToolName() {
        return ToolName.STATE_FUZZER_CLIENT;
    }
}
