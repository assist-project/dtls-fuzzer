package se.uu.it.modeltester.config;

import com.beust.jcommander.Parameter;

public class DiagnosisConfig {
	@Parameter(names = "-unsupportedStateRatio", required = false, arity = 0, description = "The ratio of states"
			+ "in which fragmentation is not supported for an input. This ratio is used "
			+ "to determine if fragmentation is globally not supported for an input")
	private double unsupportedStateRatio = 0.3;

	public DiagnosisConfig() {
	}

	public double getUnsupportedStateRatio() {
		return unsupportedStateRatio;
	}

}
