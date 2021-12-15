package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;

public class TestRunnerConfig {
	@Parameter(names = "-test", required = false, description = "Debug option, instead of learning, executes a test in the given file and exits. "
			+ "For example of test files, see './examples/tests/'."
			+ "If the file doesn't exist, it assumes the string supplied is a space-separated sequence of inputs. "
			+ "It parses and executed these inputs on the system. ")
	private String test = null;

	@Parameter(names = "-times", required = false, description = "The number of times the tests should be run")
	private Integer times = 1;
	
	@Parameter(names = "-testSpecification", required = false, description = "A model against which the resulting outputs are compared. For examples, look at './examples/models/'. "
			+ "If provided, the test will be run both on the system and on the model.")
	private String testSpecification;
	
	
	@Parameter(names = "-showTransitionSequence", required = false, description = "Show the sequence of transitions at the end in a nicer form.")
	private boolean showTransitionSequence;

	public Integer getTimes() {
		return times;
	}

	public void setTimes(Integer times) {
		this.times = times;
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}
	
	public String getTestSpecification() {
		return testSpecification;
	}
	
	public boolean isShowTransitionSequence() {
		return showTransitionSequence;
	}
	
}
