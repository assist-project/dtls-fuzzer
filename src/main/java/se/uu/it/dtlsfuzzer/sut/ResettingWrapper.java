package se.uu.it.dtlsfuzzer.sut;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import se.uu.it.dtlsfuzzer.CleanupTasks;
import se.uu.it.dtlsfuzzer.config.SulDelegate;

public class ResettingWrapper<I, O> implements SUL<I, O> {

	private SUL<I, O> sul;

	private Socket resetSocket;
	private InetSocketAddress resetAddress;
	private long resetCommandWait;
	private boolean resetAck;

	public ResettingWrapper(SUL<I, O> sul, SulDelegate sulDelegate,
			CleanupTasks tasks) {
		this.sul = sul;
		resetAddress = new InetSocketAddress(sulDelegate.getResetAddress(),
				sulDelegate.getResetPort());
		resetCommandWait = sulDelegate.getResetCommandWait();
		resetAck = sulDelegate.getResetAck();
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
				resetSocket.connect(resetAddress);
			}
			sul.pre();
			byte[] resetCmd = "reset\n".getBytes();
			byte[] ackBuf = new byte[1000];

			resetSocket.getOutputStream().write(resetCmd);
			resetSocket.getOutputStream().flush();
			if (resetAck) {
				int bytes = resetSocket.getInputStream().read(ackBuf);
				if (bytes < 0) {
					throw new RuntimeException("Server has closed the socket");
				}
			}
			if (resetCommandWait > 0)
				Thread.sleep(resetCommandWait);
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
