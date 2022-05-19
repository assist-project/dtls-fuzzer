package se.uu.it.dtlsfuzzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.io.Files;

import de.rub.nds.tlsattacker.util.UnlimitedStrengthEnabler;
import se.uu.it.dtlsfuzzer.config.StateFuzzerClientConfig;
import se.uu.it.dtlsfuzzer.config.StateFuzzerServerConfig;
import se.uu.it.dtlsfuzzer.config.TestRunnerEnabler;
import se.uu.it.dtlsfuzzer.config.TimingProbe;
import se.uu.it.dtlsfuzzer.config.TimingProbeEnabler;
import se.uu.it.dtlsfuzzer.config.ToolPropertyAwareConverterFactory;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static String ARGS_FILE = "command.args";
	
	private static final String CMD_STATE_FUZZER_CLIENT = "state-fuzzer-client";
	private static final String CMD_STATE_FUZZER_SERVER = "state-fuzzer-server";
	
	private static List<String> commands = Arrays.asList(
			CMD_STATE_FUZZER_CLIENT, 
			CMD_STATE_FUZZER_SERVER
			);
	
	
	public static void main(String args[]) throws IOException, JAXBException, XMLStreamException {
		UnlimitedStrengthEnabler.enable();
		Security.addProvider(new BouncyCastleProvider());
		int startCmd = 0;
		int endCmd = 0;
		String [] cmdArgs;
		
		if (args.length == 0) {
			// to show global usage
			processCommand(args);
		}
		
		while (args.length > endCmd) {
			startCmd = endCmd;
			while (args.length > endCmd && !args[endCmd].equals("--")) {
				endCmd ++;
			}
			cmdArgs = Arrays.copyOfRange(args, startCmd, endCmd);
			processCommand(cmdArgs);
			endCmd ++;
		}
	}
	
	private static void processCommand(String [] args) {
		StateFuzzerClientConfig stateFuzzerClientConfig = new StateFuzzerClientConfig();
		StateFuzzerServerConfig stateFuzzerServerConfig = new StateFuzzerServerConfig();
		
		JCommander commander = JCommander.newBuilder()
				.allowParameterOverwriting(true)
				.addConverterFactory(new ToolPropertyAwareConverterFactory())
				.programName("dtls-fuzzer")
				.addCommand(CMD_STATE_FUZZER_CLIENT, stateFuzzerClientConfig)
				.addCommand(CMD_STATE_FUZZER_SERVER, stateFuzzerServerConfig)
				.build();	
		commander.addConverterFactory(new ToolPropertyAwareConverterFactory());
		
		if (args.length > 0 && !commander.getCommands().containsKey(args[0]) && !args[0].startsWith("@")  && new File(args[0]).exists()) {
			LOGGER.info("The first argument is a file path. Processing it as an argument file.");
			args[0] = "@" + args[0];
		} 
		
		try {
			commander.parse(args);
			if (commander.getParsedCommand() == null) {
				showGlobalUsage(commander);
				return;
			}
			LOGGER.info("Processing command {}", commander.getParsedCommand());
			switch(commander.getParsedCommand()) {
			case CMD_STATE_FUZZER_CLIENT:
				if (stateFuzzerClientConfig.isHelp()) {
					commander.usage(commander.getParsedCommand());
					break;
				}
				stateFuzzerClientConfig.applyDelegate(null);
				debugOptionCheck(stateFuzzerClientConfig);
				
				LOGGER.info("State-fuzzing a DTLS client");
				// this is an extra step done to store the running arguments
				prepareOutputDir(args, stateFuzzerClientConfig.getOutput());
				StateFuzzer clientFuzzer = new StateFuzzer(stateFuzzerClientConfig);
				clientFuzzer.startFuzzing();
				break;
			case CMD_STATE_FUZZER_SERVER:
				if (stateFuzzerServerConfig.isHelp()) {
					commander.usage(commander.getParsedCommand());
					break;
				}
				stateFuzzerServerConfig.applyDelegate(null);
				debugOptionCheck(stateFuzzerServerConfig);
				
				LOGGER.info("State-fuzzing a DTLS server");
				// this is an extra step done to store the running arguments
				prepareOutputDir(args, stateFuzzerServerConfig.getOutput());
				StateFuzzer serverFuzzer = new StateFuzzer(stateFuzzerServerConfig);
				serverFuzzer.startFuzzing();
				break;
			}
		} catch (ParameterException E) {
			LOGGER.error("Could not parse provided parameters. " + E.getLocalizedMessage());
			LOGGER.debug(E);
			if (commander.getParsedCommand() != null) {
				commander.usage(commander.getParsedCommand());
			} else {
				showGlobalUsage(commander);
			}
		} catch (Exception E) {
			LOGGER.error("Encountered an exception. See debug for more info.");
			E.printStackTrace();
			LOGGER.error(E);
		}
	}
	
	private static void showGlobalUsage(JCommander commander) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Usage: <main class> [command] [command options] [-- [command] [command options] ]*");
		pw.println("Where command is one of the following:");
		for (String cmd : commands) {
			pw.println(cmd + "    " + commander.getCommandDescription(cmd));
		}
		
		LOGGER.info(sw.toString());
	}
	
	/*
	 * Checks if debug options have been supplied for launching the test runner/timing probe.
	 * Executes these tools and exits if that is the case.
	 */
	private static void debugOptionCheck(TestRunnerEnabler config) throws IOException {
		if (config.getTestRunnerConfig().getTest() != null) {
			LOGGER.info("Debug operation is engaged");
			if (config instanceof TimingProbeEnabler && ((TimingProbeEnabler) config).getTimingProbe().isActive()) {
				LOGGER.info("Running timing probe");
				TimingProbe.runTimingProbe((TimingProbeEnabler) config);
			} else {
				LOGGER.info("Running test runner");
				TestRunner.runTestRunner(config);
			}
			System.exit(0);
		}
	}
	
	/*
	 * Creates the output directory in advance in order to store in it the arguments file before the tool is executed. 
	 */
	private static void prepareOutputDir(String args [], String dirPath) {
		File outputFolder = new File(dirPath);
		outputFolder.mkdirs();

		try {
			copyArgsToOutDir(args, dirPath);
		} catch (IOException E) {
			LOGGER.error("Failed to copy arguments file");
			E.printStackTrace();
			LOGGER.error(E);
		}			
	}

	/*
	 * Generates a file comprising the entire command given to to fuzzer.
	 */
	private static void copyArgsToOutDir(String[] args, String outDir) throws IOException {
		FileOutputStream fw = new FileOutputStream(new File(outDir, ARGS_FILE));
		PrintStream ps = new PrintStream(fw);
		for (String arg : args) {
			if (arg.startsWith("@")) {
				String argsFileName = arg.substring(1);
				File argsFile = new File(argsFileName);
				if (!argsFile.exists()) {
					LOGGER.warn("Arguments file " + argsFile + "has been moved ");
				} else {
					Files.copy(argsFile, fw);
				}
			} else {
				ps.println(arg);
			}
		}
		ps.close();
		fw.close();
	}
}
