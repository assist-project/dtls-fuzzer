package se.uu.it.modeltester.config;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import de.rub.nds.tlsattacker.core.config.TLSDelegateConfig;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;

public class ModelBasedTesterConfig {

	@Parameter(names = "-specification", required = false, description = "A model of the specification. For examples, look at './models/'. "
			+ "If no specification is given, active learning is run with the provided alphabet."
			+ "Inputs in the specification are defined in the alphabet, outputs are parsed as strings. "
			+ "Outputs are capitalized snake case of the messages involved. ")
	private String specification = null;

	@Parameter(names = "-onlyLearn", required = false, description = "Only generates a model of the specification. Does not do conformance testing")
	private boolean onlyLearn = false;

	@Parameter(names = "-inputs", required = false, description = "A list of comma separated input names. "
			+ "If given, testing will be restricted to these inputs. ")
	private List<String> inputs = null;

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
	private TestingConfig testingConfig;

	@ParametersDelegate
	private TestRunnerConfig testRunnerConfig;
	
	@ParametersDelegate
	private GeneralDelegate generalDelegate;

	

	public ModelBasedTesterConfig() {
		generalDelegate = new GeneralDelegate();
		sulDelegate = new SulDelegate();
		learningConfig = new LearningConfig();
		testingConfig = new TestingConfig();
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

	public TestingConfig getTestingConfig() {
		return testingConfig;
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

	public boolean isOnlyLearn() {
		return onlyLearn;
	}
}
