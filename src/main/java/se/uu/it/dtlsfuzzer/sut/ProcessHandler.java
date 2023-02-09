package se.uu.it.dtlsfuzzer.sut;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.NullOutputStream;

import se.uu.it.dtlsfuzzer.config.SulDelegate;

/**
 * Allows one to start/stop processes launched by executing a given command. At
 * most one process can be executing at a time.
 *
 * @author Paul
 */
public class ProcessHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	// the maximum amount of time we wait for a launched process to terminate
	private static final long TERM_WAIT_MS = 1000;

	private final ProcessBuilder pb;
	private Process currentProcess;
	private String terminateCommand;
	private OutputStream output;
	private OutputStream error;
	private long startWait;
	private boolean hasLaunched;

	private ProcessHandler(String command, long startWait) {
		// '+' after \\s takes care of multiple consecutive spaces so that they
		// don't result in empty arguments
		pb = new ProcessBuilder(command.split("\\s+"));
		this.startWait = startWait;
		LOGGER.info("Command to launch SUT: {}", command);
		output = NullOutputStream.getInstance();
		error = NullOutputStream.getInstance();
	}

	public ProcessHandler(SulDelegate sulConfig) {
		this(sulConfig.getCommand(), sulConfig.getStartWait());
		if (sulConfig.getProcessDir() != null) {
			setDirectory(new File(sulConfig.getProcessDir()));
		}
		if (sulConfig.isRedirectOutputStreams()) {
			output = System.out;
			error = System.err;
		}
		terminateCommand = sulConfig.getTerminateCommand();
		if (terminateCommand != null) {
			LOGGER.info("Command to terminate SUT: {}", terminateCommand);
		}
	}

	public void redirectOutput(OutputStream toOutput) {
		output = toOutput;
	}

	public void redirectError(OutputStream toOutput) {
		error = toOutput;
	}

	public void setDirectory(File procDir) {
		pb.directory(procDir);
	}

	/**
	 * Launches a process which executes the handler's command. Does nothing if
	 * the process has been launched already.
	 *
	 * Sets {@link ProcessHandler#hasLaunched} to true on successful launch of
	 * the process, making {@link ProcessHandler#hasLaunched()} return true
	 * thereafter.
	 *
	 * After launching, it sleeps for {@link ProcessHandler#startWait}
	 * milliseconds.
	 */
	public void launchProcess() {
		try {
			if (currentProcess == null) {
				hasLaunched = true;
				currentProcess = pb.start();
				pipe(currentProcess.getInputStream(), output);
				pipe(currentProcess.getErrorStream(), error);
				if (startWait > 0) {
					Thread.sleep(startWait);
				}
			} else {
				LOGGER.warn("Process has already been started");
			}

		} catch (IOException | InterruptedException E) {
			LOGGER.error("Couldn't start process due to exec:", E);
			throw new RuntimeException(E);
		}
	}

	/**
	 * Terminates the process executing the handler's command. Does nothing if
	 * the process has been terminated already.
	 */
	public void terminateProcess() {
		if (currentProcess != null) {
			if (terminateCommand != null) {
				try {
					Runtime.getRuntime().exec(terminateCommand);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				currentProcess.destroyForcibly();
			}
            try {
                long time = System.currentTimeMillis();
                while ( System.currentTimeMillis() - time < TERM_WAIT_MS && currentProcess.isAlive()) {
                    Thread.sleep(1);
                }

                if (currentProcess.isAlive()) {
                    throw new RuntimeException(String.format("SUT process still alive after %s ms", TERM_WAIT_MS));
                }
            } catch ( InterruptedException E) {
                LOGGER.error("Interrupted while waiting for process to terminate", E);
                throw new RuntimeException(E);
            }
			currentProcess = null;
		} else {
			LOGGER.debug("Process has already terminated");
		}
	}

	public boolean isAlive() {
		return currentProcess != null && currentProcess.isAlive();
	}

	/**
	 * Returns true if the process has been launched successfully at least once,
	 * irrespective of whether it has terminated since first execution.
	 */
	public boolean hasLaunched() {
		return hasLaunched;
	}

	private void pipe(final InputStream src, final OutputStream dest) {
		new Thread(new Runnable() {
			public void run() {
			    PrintStream psDest = new PrintStream(dest);
				Scanner sc = new Scanner(src);
				while (sc.hasNextLine()) {
				    psDest.println(sc.nextLine());
				    psDest.flush();
				}
				sc.close();
			}
		}).start();
	}

}
