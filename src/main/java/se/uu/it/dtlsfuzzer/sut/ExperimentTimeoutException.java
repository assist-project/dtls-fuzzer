package se.uu.it.dtlsfuzzer.sut;

import java.time.Duration;

public class ExperimentTimeoutException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Duration duration;

	public ExperimentTimeoutException(Duration duration) {
		super("Experiment has exceeded the duration limit given: "
				+ duration.toString());
		this.duration = duration;
	}

	public Duration getDuration() {
		return duration;
	}
}
