package se.uu.it.modeltester.config;

import com.beust.jcommander.Parameter;

public class TestingConfig {
	@Parameter(names = "-trace", required = false, description = "Debug option, runs the inputs in the given file and exits. ")
    private String trace = null;
	
	@Parameter(names = "-times", required = false, description = "The number of times the inputs should be run")
    private Integer times = 1;


	public Integer getTimes() {
		return times;
	}

	public void setTimes(Integer times) {
		this.times = times;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}
}
