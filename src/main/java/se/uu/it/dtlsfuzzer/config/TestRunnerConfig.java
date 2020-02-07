package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;

public class TestRunnerConfig {
	@Parameter(names = "-test", required = false, description = "Debug option, instead of learning, executes a test in the given file and exits. For example of test files, see './examples/tests/'.")
	private String test = null;

	@Parameter(names = "-times", required = false, description = "The number of times the tests should be run")
	private Integer times = 1;

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
}
