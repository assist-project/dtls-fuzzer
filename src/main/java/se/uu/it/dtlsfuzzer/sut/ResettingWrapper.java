package se.uu.it.dtlsfuzzer.sut;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import se.uu.it.dtlsfuzzer.config.SulDelegate;

public class ResettingWrapper<I, O> implements SUL<I, O> {

	private SUL<I, O> sul;

	private DatagramSocket resetSocket;
	private InetSocketAddress resetAddress;
	private long resetCommandWait;

	public ResettingWrapper(SUL<I, O> sul, SulDelegate sulDelegate) {
		this.sul = sul;
		resetAddress = new InetSocketAddress(sulDelegate.getResetAddress(),
				sulDelegate.getResetPort());
		resetCommandWait = sulDelegate.getResetCommandWait();
		try {
			resetSocket = new DatagramSocket();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void pre() {
		sul.pre();
		byte[] buf = "reset".getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length,
				resetAddress);
		try {
			resetSocket.send(packet);
			if (resetCommandWait > 0)
				Thread.sleep(resetCommandWait);
		} catch (IOException e) {
			Runtime.getRuntime().runFinalization();
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
