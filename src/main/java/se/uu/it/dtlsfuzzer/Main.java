package se.uu.it.dtlsfuzzer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
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
	private static String ERROR_FILE = "error.msg";

	public static void main(String args[]) throws IOException {
		UnlimitedStrengthEnabler.enable();
		Security.addProvider(new BouncyCastleProvider());
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

			try {
				DtlsFuzzer tester = new DtlsFuzzer(config);
				tester.startTesting();
				// this is an extra step done to store the running arguments
				if (config.getOutput() != null
						&& new File(config.getOutput()).exists()) {
					copyArgsToOutDir(args, config.getOutput());
				}
			} catch (Exception E) {
				LOGGER.error("Encountered an exception. See debug for more info.");
				E.printStackTrace();
				// TODO ^^ what says here :)
				LOGGER.error(E);

				File outputFolder = new File(config.getOutput());
				if (outputFolder.exists()) {
					// useful to log what actually went wrong
					try (FileWriter fw = new FileWriter(new File(outputFolder,
							ERROR_FILE))) {
						PrintWriter pw = new PrintWriter(fw);
						pw.println(E.getMessage());
						E.printStackTrace(pw);
						pw.close();
					}
				}
			}
		} catch (ParameterException E) {
			LOGGER.error("Could not parse provided parameters. "
					+ E.getLocalizedMessage());
			LOGGER.debug(E);
			commander.usage();
		}
	}

	private static void copyArgsToOutDir(String[] args, String outDir)
			throws IOException {
		File file = Paths.get(outDir, ARGS_FILE).toFile();
		for (String arg : args) {
			if (arg.startsWith("@")) {
				String argsFileName = arg.substring(1);
				File argsFile = new File(argsFileName);
				if (!argsFile.exists()) {
					LOGGER.error("Arguments file " + argsFile
							+ "has been moved ");
				} else {
					Files.copy(argsFile, file);
				}
			} 
		}
	}
}
