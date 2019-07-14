package se.uu.it.dtlsfuzzer.config;

import java.util.Arrays;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;

public class ProtocolVersionConverter
		implements
			IStringConverter<ProtocolVersion> {

	@Override
	public ProtocolVersion convert(String value) {
		try {
			return ProtocolVersion.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new ParameterException("Value " + value
					+ " cannot be converted to a ProtocolVersion. "
					+ "Available values are: "
					+ Arrays.toString(ProtocolVersion.values()));
		}
	}

}
