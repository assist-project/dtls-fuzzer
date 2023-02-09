package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public abstract class StateFuzzerConfig extends ToolConfig implements TimingProbeEnabler, TestRunnerEnabler, AlphabetOptionProvider, RoleProvider, SulDelegateProvider {

	@Parameter(names = "-alphabet", required = false, description = "An .xml file defining the input alphabet. "
			+ "The alphabet is used to interpret inputs from a given specification, as well as to learn. "
			+ "Each input in the alphabet has a name under which it appears in the specification."
			+ "The name is defaulted mostly to the capitalized snake case of the message involved."
			+ "For example, for <FinishedInput/> the name is FINISHED."
			+ "The name can be changed by setting the 'name' attribute")
	private String alphabet = null;

	@Parameter(names = "-output", required = false, description = "The folder in which results should be saved")
	private String output = "output";

	@ParametersDelegate
	private LearningConfig learningConfig;

	@ParametersDelegate
	private MapperConfig mapperConfig;

	@ParametersDelegate
	private TestRunnerConfig testRunnerConfig;

	@ParametersDelegate
	private TimingProbe timingProbe;

	public StateFuzzerConfig() {
		learningConfig = new LearningConfig();
		testRunnerConfig = new TestRunnerConfig();
		timingProbe = new TimingProbe();
		mapperConfig = new MapperConfig();
	}

	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setLearningConfig(LearningConfig learningConfig) {
		this.learningConfig = learningConfig;
	}

	public void setMapperConfig(MapperConfig mapperConfig) {
		this.mapperConfig = mapperConfig;
	}

	public LearningConfig getLearningConfig() {
		return learningConfig;
	}

	public TestRunnerConfig getTestRunnerConfig() {
		return testRunnerConfig;
	}

	public String getOutput() {
		return output;
	}

	public String getAlphabet() {
		return alphabet;
	}

	public TimingProbe getTimingProbe() {
		return timingProbe;
	}

	public MapperConfig getMapperConfig() {
		return mapperConfig;
	}
}
