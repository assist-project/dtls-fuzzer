package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import java.util.HashMap;
import java.util.Map;


public class ToolPropertyAwareConverterFactory implements IStringConverterFactory {

	private Map<Class,Class<? extends IStringConverter>> converters = new HashMap<>();

	public ToolPropertyAwareConverterFactory() {
		converters.put(String.class, FromStringConverter.class);
		converters.put(Integer.class, FromIntegerConverter.class);
		converters.put(Long.class, FromLongConverter.class);
	}

	@Override
	public Class<? extends IStringConverter<?>> getConverter(Class<?> forType) {
		Class<? extends IStringConverter<?>>factory = (Class<? extends IStringConverter<?>>) converters.get(forType);
 		return factory;
	}

	private static class FromStringConverter implements IStringConverter<String> {
		@Override
		public String convert(String value) {
			return ToolConfig.resolve(value);
		}
	}

	private static class FromIntegerConverter implements IStringConverter<Integer> {
		@Override
		public Integer convert(String value) {
			return Integer.valueOf(ToolConfig.resolve(value.trim()));
		}
	}

	private static class FromLongConverter implements IStringConverter<Long> {
		@Override
		public Long convert(String value) {
			return Long.valueOf(ToolConfig.resolve(value.trim()));
		}
	}

}
