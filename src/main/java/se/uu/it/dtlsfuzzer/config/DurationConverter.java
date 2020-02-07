package se.uu.it.dtlsfuzzer.config;

import java.time.Duration;

import com.beust.jcommander.IStringConverter;

public class DurationConverter implements IStringConverter<Duration> {

	@Override
	public Duration convert(String value) {
		return Duration.parse(value);
	}

}
