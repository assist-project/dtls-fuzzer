package se.uu.it.dtlsfuzzer.sut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import se.uu.it.dtlsfuzzer.CleanupTasks;
import se.uu.it.dtlsfuzzer.config.SulDelegate;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

/**
 * The role of the resetting wrapper is to communicate with a SUL wrapper over a
 * TCP connection. The resetting wrapper sends "reset" commands to the SUL
 * wrapper. The SUL wrapper reacts by terminating the current SUL instance,
 * starting a new one and responding with the fresh port number the instance
 * listens to. This response is also used as a form of acknowledgement, telling
 * the learning setup that the new instance is ready to receive messages.
 * 
 * Setting the port dynamically (rather than binding it statically) proved
 * necessary in order to avoid port collisions.
 */
public class ResettingClientWrapper implements SUL<TlsInput, TlsOutput> {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final String CMD_RESET = "reset";
	private static final String RESP_STOPPED = "stopped";

	private SUL<TlsInput, TlsOutput> sul;

	private Socket resetSocket;
	private InetSocketAddress resetAddress;
	private long resetCommandWait;
	private BufferedReader reader;
	private PrintWriter writer;


	public ResettingClientWrapper(SUL<TlsInput, TlsOutput> sul, SulDelegate sulDelegate,
			CleanupTasks tasks) {
		this.sul = sul;
		resetAddress = new InetSocketAddress(sulDelegate.getResetAddress(),
				sulDelegate.getResetPort());
		resetCommandWait = sulDelegate.getResetCommandWait();
		try {
			resetSocket = new Socket();
			resetSocket.setReuseAddress(true);
			resetSocket.setSoTimeout(30000);
			tasks.submit(new Runnable() {
				@Override
				public void run() {
					try {
						if (!resetSocket.isClosed()) {
							System.out.println("Closing socket");
							resetSocket.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void pre() {
		try {
			if (!resetSocket.isConnected()) {
				if (resetCommandWait > 0)
					Thread.sleep(resetCommandWait);
				resetSocket.connect(resetAddress);
				reader = new BufferedReader(new InputStreamReader(
						resetSocket.getInputStream()));
				writer = new PrintWriter(new OutputStreamWriter(resetSocket.getOutputStream()));
			}
			writer.println(CMD_RESET);
			writer.flush();
			String ack = reader.readLine();
			if (ack == null) {
				throw new RuntimeException("Server has closed the socket");
			}
			
			LOGGER.debug("Client connecting");

			/*
			 * We have to pre before the SUT does, so we have a port available
			 * for it.
			 */

			sul.pre();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void post() {
		sul.post();
	}

	@Override
	public TlsOutput step(TlsInput in) throws SULException {
		TlsOutput output = sul.step(in);
		try {
			if (reader.ready()) {
				String response = reader.readLine();
				if (response.equals(RESP_STOPPED)) {
					LOGGER.debug("Server stopped");
					output.setAlive(false);
				} else {
					throw new RuntimeException("Received invalid response");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return output;
	}
}
