package se.uu.it.dtlsfuzzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.io.Files;

import de.rub.nds.tlsattacker.util.UnlimitedStrengthEnabler;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger(Main.class
			.getName());

	private static String ARGS_FILE = "command.args";

	public static void main(String args[]) throws IOException {
		// necessary for TLS-Attacker to function
		UnlimitedStrengthEnabler.enable();
		Security.addProvider(new BouncyCastleProvider());

		// parse arguments
		DtlsFuzzerConfig config = new DtlsFuzzerConfig();
		JCommander commander = new JCommander(config);
		commander.setAllowParameterOverwriting(true);
		try {
			commander.parse(args);
			if (config.getGeneralDelegate().isHelp()) {
				commander.usage();
				return;
			}
			config.getGeneralDelegate().applyDelegate(null);
			File outputFolder = new File(config.getOutput());
			outputFolder.mkdirs();

			try {
				// this is an extra step done to store the running arguments
				copyArgsToOutDir(args, config.getOutput());
				DtlsFuzzer tester = new DtlsFuzzer(config);
				tester.startTesting();
			} catch (Exception E) {
				LOGGER.error("Encountered an exception. See debug for more info.");
				E.printStackTrace();
				LOGGER.error(E);
			}
		} catch (ParameterException E) {
			LOGGER.error("Could not parse provided parameters. "
					+ E.getLocalizedMessage());
			LOGGER.debug(E);
			commander.usage();
		}
	}

	/*
	 * Generates a file comprising the entire command given to to fuzzer.
	 */
	private static void copyArgsToOutDir(String[] args, String outDir)
			throws IOException {
		FileOutputStream fw = new FileOutputStream(new File(outDir, ARGS_FILE));
		PrintStream ps = new PrintStream(fw);
		for (String arg : args) {
			if (arg.startsWith("@")) {
				String argsFileName = arg.substring(1);
				File argsFile = new File(argsFileName);
				if (!argsFile.exists()) {
					LOGGER.error("Arguments file " + argsFile
							+ "has been moved ");
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
