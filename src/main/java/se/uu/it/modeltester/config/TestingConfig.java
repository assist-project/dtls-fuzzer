package se.uu.it.modeltester.config;

import com.beust.jcommander.Parameter;

public class TestingConfig {
	@Parameter(names = "-exhaustive", required = false, arity=0, description = "If provided, testing a state is performed for all distinguishing suffixes, "
			+ "and is not stopped once non-conformance is detected for a suffix")
	private Boolean isExhaustive = Boolean.FALSE;
	
	@Parameter(names = "-bound", required = false, description = "An optional bound on the total number of tests")
    private Long bound = null;
	
	@Parameter(names = "-fromNumFrag", required = false, arity=0, description = "Indicates the number from which to start fragmenting messages.")
	private int fromNumFrag = 2;
	
	@Parameter(names = "-toNumFrag", required = false, arity=0, description = "Indicates the number to which (non-inclusive) to fragment messages.")
	private int toNumFrag = 4;
	
	@Parameter(names = "-generateTests", required = false, arity=0, description = "For every bug generate a test file which can be executed via the TestRunner utility.")
	private boolean isTestGenerationEnabled = false;

	public Boolean isExhaustive() {
		return isExhaustive;
	}

	public Long getBound() {
		return bound;
	}

	public int getFromNumFrag() {
		return fromNumFrag;
	}

	public int getToNumFrag() {
		return toNumFrag;
	}
	
	public boolean isTestGenerationEnabled() {
		return isTestGenerationEnabled;
	}
	
//	public boolean 
}
