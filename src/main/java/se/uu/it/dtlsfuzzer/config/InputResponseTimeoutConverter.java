package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class InputResponseTimeoutConverter implements IStringConverter<InputResponseTimeoutMap> {

	@Override
	public InputResponseTimeoutMap convert(String value) {
		InputResponseTimeoutMap inputResponseTimeout = new InputResponseTimeoutMap();
		String[] inputValuePairs = value.split("\\,");
		
		for (String inputValuePair : inputValuePairs) {
			String[] split = inputValuePair.split("\\:");
			if (split.length != 2) {
				throw new ParameterException(errMessage(value));
			} else {
				try {
				inputResponseTimeout.put(split[0], Long.valueOf(split[1]));
				} catch(Exception e) {
					throw new ParameterException(errMessage(value), e);
				}
			}
		}
		
		return inputResponseTimeout;
	}
	
	private String errMessage(String value) {
		return String.format("Error processing InputResponseTimeoutMap from \"%s\". "
				+ "Expected format: \"input1:value1,input2:value2...\"; e.g. \"SERVER_HELLO_DONE:100\" ", value);
	}

}
