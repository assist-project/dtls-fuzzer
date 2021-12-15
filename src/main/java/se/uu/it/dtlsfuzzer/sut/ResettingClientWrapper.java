package se.uu.it.dtlsfuzzer.sut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import se.uu.it.dtlsfuzzer.CleanupTasks;
import se.uu.it.dtlsfuzzer.config.SulDelegate;

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
public class ResettingClientWrapper<I, O> implements SUL<I, O> {

	private static final Logger LOGGER = LogManager
			.getLogger(ResettingClientWrapper.class);

	private SUL<I, O> sul;

	private Socket resetSocket;
	private InetSocketAddress resetAddress;
	private long resetCommandWait;
	private BufferedReader reader;

	public ResettingClientWrapper(SUL<I, O> sul, SulDelegate sulDelegate,
			CleanupTasks tasks) {
		this.sul = sul;
		resetAddress = new InetSocketAddress(sulDelegate.getResetAddress(),
				sulDelegate.getResetPort());
		resetCommandWait = sulDelegate.getResetCommandWait();
		try {
			resetSocket = new Socket();
			resetSocket.setReuseAddress(true);
			resetSocket.setSoTimeout(20000);
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
			}
			byte[] resetCmd = "reset\n".getBytes();

			resetSocket.getOutputStream().write(resetCmd);
			resetSocket.getOutputStream().flush();
			String ack = reader.readLine();
			if (ack == null) {
				throw new RuntimeException("Server has closed the socket");
			}
			
			LOGGER.info("Client connecting");

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
	public O step(I in) throws SULException {
		return sul.step(in);
	}
}
