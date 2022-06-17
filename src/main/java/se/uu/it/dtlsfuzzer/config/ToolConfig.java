package se.uu.it.dtlsfuzzer.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.beust.jcommander.DynamicParameter;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.GeneralDelegate;

public class ToolConfig extends GeneralDelegate {
	
	public void applyDelegate(Config config) {
		super.applyDelegate(config);
		if (isDebug()) {
			Configurator.setAllLevels("se.uu.it.dtlsfuzzer", Level.DEBUG);
			Configurator.setAllLevels("de.rub.nds.tlsattacker", Level.INFO);
	    } else if (isQuiet()) {
			Configurator.setAllLevels("se.uu.it.dtlsfuzzer", Level.OFF);
			Configurator.setAllLevels("de.rub.nds.tlsattacker", Level.OFF);
	    } else {
	         Configurator.setAllLevels("se.uu.it.dtlsfuzzer", Level.INFO);
	         Configurator.setAllLevels("de.rub.nds.tlsattacker", Level.ERROR);
	    }
	}
	
	public static final String FUZZER_PROPS = "fuzzer.properties";
	public static final String DEFAULT_FUZZER_PROPS = "dtls-fuzzer.properties";
	
	public static final String FUZZER_DIR = "fuzzer.dir";
	public static final String SUTS_DIR = "suts.dir";
	public static final String SUT_PORT = "sut.port";
	public static final String FUZZER_PORT = "fuzzer.port";
	
	/* Stores system properties*/
	@DynamicParameter(names = "-D", description = "Definitions for variables, which can be refered to in arguments by ${var}. "
			+ "Variables are replaced with their corresponding values before the arguments are parsed."
			+ "Can be passed as either JVM properties (after java) or as application properties.")
	protected static Map<String, String> props = new LinkedHashMap<>();
	
	private static Map<String, String> originalProps = new LinkedHashMap<>();
	
	// initialize system properties
	static {
		Properties fuzzerProps = new Properties();
		String fuzzerPropsLocation = DEFAULT_FUZZER_PROPS;
		try {
			if (System.getProperty(FUZZER_PROPS) == null) {
				InputStream resource = ToolConfig.class.getClassLoader().getResourceAsStream(DEFAULT_FUZZER_PROPS);
				fuzzerProps.load(resource);
			} else {
				fuzzerProps.load(new FileReader(fuzzerPropsLocation));
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not load properties");
		}
		
		for (String propName : fuzzerProps.stringPropertyNames()) {
			String systemPropValue = System.getProperty(propName);
			if (systemPropValue != null) {
				props.put(propName, systemPropValue);
			} else {
				props.put(propName, fuzzerProps.getProperty(propName));
			}
		}
		
		String fuzzerDir = System.getProperty(FUZZER_DIR);
		if (fuzzerDir == null) {
			fuzzerDir = System.getProperty("user.dir");
		}
		props.put(FUZZER_DIR, fuzzerDir);
		
		String sutsDir = fuzzerProps.getProperty(SUTS_DIR);
		if (sutsDir == null) {
			sutsDir = fuzzerDir + File.separator + "suts";
		}
		props.put(SUTS_DIR, sutsDir);
		
		/*
		 * Sut port: between 10000 and 39999
		 */
		String sutPort = fuzzerProps.getProperty(SUT_PORT);
		if (sutPort == null) {
			long sutSec = (System.currentTimeMillis() / 1000 % 30000) + 10000;
			sutPort = Long.toString(sutSec);
		}
		props.put(SUT_PORT, sutPort);
		
		/*
		 * Fuzzer port: between 40000 and 65535 (= 0xFFFF or max port)
		 */
		String fuzzerPort = fuzzerProps.getProperty(FUZZER_PORT);
		if (fuzzerPort == null) {
			long fuzzSec = (System.currentTimeMillis() / 1000 % 25536) + 40000;
			fuzzerPort = Long.toString(fuzzSec);
		}
		props.put(FUZZER_PORT, fuzzerPort);
		originalProps.putAll(props);
	}
	
	/**
     * Returns true if due to placeholder variables, an additional parsing is required.
     */
    public static boolean isReparseRequired() {
        return !originalProps.equals(props);
    }
	
	// so we don't replaceAll each time
	private static Map<String, String> resolutionCache = new HashMap<>();
	
	/**
	 * Resolves are the system properties in a given user string.
	 */
	public static String resolve(String userString) {
		if (userString == null) {
			return null;
		}
		
		if (resolutionCache.containsKey(userString)) {
			return resolutionCache.get(userString);
		}
		
		String resolvedStr = userString;
		for (Map.Entry<String,String> prop : props.entrySet()) {
			resolvedStr = resolvedStr.replaceAll("\\$\\{"+prop.getKey()+"\\}", prop.getValue());
		}
		return resolvedStr;
	}
}
