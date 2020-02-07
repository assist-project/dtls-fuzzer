package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;

public class DtlsFuzzerConfig {

	@Parameter(names = "-specification", required = false, description = "A model of the specification. For examples, look at './examples/specifications/'. "
			+ "If a debug test is executed (via -test), the test will be run both on the system and on the model.")
	private String specification = null;


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
	private SulDelegate sulDelegate;

	@ParametersDelegate
	private LearningConfig learningConfig;

	@ParametersDelegate
	private TestRunnerConfig testRunnerConfig;

	@ParametersDelegate
	private GeneralDelegate generalDelegate;

	public DtlsFuzzerConfig() {
		generalDelegate = new GeneralDelegate();
		sulDelegate = new SulDelegate();
		learningConfig = new LearningConfig();
		testRunnerConfig = new TestRunnerConfig();
	}

	public GeneralDelegate getGeneralDelegate() {
		return generalDelegate;
	}

	public SulDelegate getSulDelegate() {
		return sulDelegate;
	}

	public LearningConfig getLearningConfig() {
		return learningConfig;
	}


	public TestRunnerConfig getTestRunnerConfig() {
		return testRunnerConfig;
	}

	public String getSpecification() {
		return specification;
	}

	public String getOutput() {
		return output;
	}

	public String getAlphabet() {
		return alphabet;
	}

}
