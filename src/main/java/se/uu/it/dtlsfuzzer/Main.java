package se.uu.it.dtlsfuzzer;

import static se.uu.it.dtlsfuzzer.config.ToolName.STATE_FUZZER_CLIENT;
import static se.uu.it.dtlsfuzzer.config.ToolName.STATE_FUZZER_SERVER;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Security;
import java.util.Arrays;

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
import se.uu.it.dtlsfuzzer.config.StateFuzzerConfig;
import se.uu.it.dtlsfuzzer.config.StateFuzzerServerConfig;
import se.uu.it.dtlsfuzzer.config.TestRunnerEnabler;
import se.uu.it.dtlsfuzzer.config.TimingProbe;
import se.uu.it.dtlsfuzzer.config.TimingProbeEnabler;
import se.uu.it.dtlsfuzzer.config.ToolConfig;
import se.uu.it.dtlsfuzzer.config.ToolPropertyAwareConverterFactory;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static String ARGS_FILE = "command.args";
	
	
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

	 /*
     * Parses arguments returning a result containing the JCommander instance used for parsing and tool configurations.
     * Returns null if parsing found errors.
     */
    protected static ParsingResult parseArguments(String args[]) {
        return parseArguments(args, false);
    }

    private static ParsingResult parseArguments(String args[], boolean reparse) {
        StateFuzzerClientConfig stateFuzzerClientConfig = new StateFuzzerClientConfig();
        StateFuzzerServerConfig stateFuzzerServerConfig = new StateFuzzerServerConfig();

        JCommander commander = JCommander.newBuilder()
                .allowParameterOverwriting(true)
                .addConverterFactory(new ToolPropertyAwareConverterFactory())
                .programName("dtls-fuzzer")
                .addCommand(STATE_FUZZER_CLIENT.getName(), stateFuzzerClientConfig)
                .addCommand(STATE_FUZZER_SERVER.getName(), stateFuzzerServerConfig)
                .build();
        commander.addConverterFactory(new ToolPropertyAwareConverterFactory());

        if (args.length > 0 && !commander.getCommands().containsKey(args[0]) && !args[0].startsWith("@")  && new File(args[0]).exists()) {
            LOGGER.info("The first argument is a file path. Processing it as an argument file.");
            args[0] = "@" + args[0];
        }

        try {
            commander.parse(args);
        } catch (ParameterException E) {
            LOGGER.error("Could not parse provided parameters. " + E.getLocalizedMessage());
            LOGGER.debug(E);
            if (commander.getParsedCommand() != null) {
		JCommander cmdCommander = commander.getCommands().get(commander.getParsedCommand());
                System.out.println(commander.getParsedCommand());
                cmdCommander.usage();
            } else {
                showGlobalUsage(commander);
            }
            return null;
        }

        if (!reparse && ToolConfig.isReparseRequired()) {
            LOGGER.info("Parsing arguments again since they alter placeholder variables");
            return parseArguments(args, true);
        }

        return new ParsingResult(stateFuzzerClientConfig, stateFuzzerServerConfig, commander);
    }

	private static void processCommand(String [] args) {
	    ParsingResult result = parseArguments(args);
	    if (result == null) {
	        return;
	    }

        JCommander commander = result.getCommander();
        if (commander.getParsedCommand() == null) {
            showGlobalUsage(commander);
            return;
        }

		try {
			LOGGER.info("Processing command {}", commander.getParsedCommand());
			StateFuzzerConfig stateFuzzerConfig = result.getParsedConfig();
			if (stateFuzzerConfig.isHelp()) {
                commander.usage();
            }
            stateFuzzerConfig.applyDelegate(null);
            testRunnerOptionCheck(stateFuzzerConfig);
            LOGGER.info("State-fuzzing a DTLS " + (stateFuzzerConfig.isClient() ? "client" : "server"));
            // this is an extra step done to store the running arguments
            prepareOutputDir(args, stateFuzzerConfig.getOutput());
            StateFuzzer stateFuzzer = new StateFuzzer(stateFuzzerConfig);
            stateFuzzer.startFuzzing();
		} catch (Exception E) {
			LOGGER.error("Encountered an exception. See debug for more info.");
			E.printStackTrace();
			LOGGER.error(E);
		}
	}
	
	/*
	 * Gives a description for each supported command.
	 */
	private static void showGlobalUsage(JCommander commander) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("Usage: <main class> [command] [command options] [-- [command] [command options] ]*");
		pw.println("Where command is one of the following:");
		for (String cmd : commander.getCommands().keySet()) {
			pw.println(cmd + "    " + commander.getUsageFormatter().getCommandDescription(cmd));
		}
		LOGGER.info(sw.toString());
	}
	
	/*
	 * Checks if options have been supplied for launching the test runner/timing probe.
	 * Executes these tools and exits if that is the case.
	 */
	private static void testRunnerOptionCheck(TestRunnerEnabler config) throws IOException {
		if (config.getTestRunnerConfig().getTest() != null) {
//			LOGGER.info("Test runner is engaged");
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
