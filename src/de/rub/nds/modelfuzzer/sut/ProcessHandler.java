package de.rub.nds.modelfuzzer.sut;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rub.nds.modelfuzzer.config.SulDelegate;

/**
 * Allows one to start/stop processes launched by executing a given command.
 * At most one process can be executing at a time.
 * @author Paul
 */
public class ProcessHandler {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final ProcessBuilder pb;
	private Process currentProcess;
	private OutputStream output;
	private OutputStream error;
	private long runWait;	

	public ProcessHandler(String command, long runWait) {
		pb = new ProcessBuilder(command.split("\\s"));
		this.runWait = runWait;
	}
	
	public ProcessHandler(SulDelegate sulConfig) {
		this(sulConfig.getCommand(), sulConfig.getRunWait());
	}
	
	public void redirectOutput(OutputStream toOutput) {
		output = toOutput;
	}
	
	public void redirectError(OutputStream toOutput) {
		error = toOutput;
	}
	
	public long getRunWait() {
		return runWait;
	}

	public void setRunWait(long runWait) {
		this.runWait = runWait;
	}
	
	/**
	 * Launches a process which executes the handler's command. Does nothing if the process has been launched already.
	 */
	public void launchProcess() {
		try {
			if (currentProcess == null) {
				currentProcess = pb.start();
				if (output != null)
					inheritIO(currentProcess.getInputStream(), new PrintStream(output));
				if (error != null)
					inheritIO(currentProcess.getErrorStream(), new PrintStream(error));
				if (runWait > 0)
					Thread.sleep(runWait);
			} else {
				LOGGER.warn("Process has already been started");
			}
			
		} catch (IOException | InterruptedException E) {
			LOGGER.error("Couldn't start process due to exec:", E);
		}
	}
	
	/**
	 * Terminates the process executing the handler's command. Does nothing if the process has been terminated already.
	 */
	public void terminateProcess() {
		if (currentProcess != null) {
			currentProcess.destroyForcibly();
			currentProcess = null;
		} else {
			LOGGER.warn("Process has already been ended");
		}
	}
	
	private void inheritIO(final InputStream src, final PrintStream dest) {
	    new Thread(new Runnable() {
	        public void run() {
	            Scanner sc = new Scanner(src);
	            while (sc.hasNextLine()) {
	                dest.println(sc.nextLine());
	            }
	            sc.close();
	        }
	    }).start();
	}
	
}
