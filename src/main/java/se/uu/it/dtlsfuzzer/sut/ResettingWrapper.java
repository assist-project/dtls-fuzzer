package se.uu.it.dtlsfuzzer.sut;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import se.uu.it.dtlsfuzzer.config.SulDelegate;

public class ResettingWrapper<I, O> implements SUL<I, O> {

	private SUL<I, O> sul;

	private DatagramSocket resetSocket;
	private InetSocketAddress resetAddress;

	public ResettingWrapper(SUL<I, O> sul, SulDelegate sulDelegate) {
		this.sul = sul;
		resetAddress = new InetSocketAddress(sulDelegate.getResetAddress(),
				sulDelegate.getResetPort());
		try {
			resetSocket = new DatagramSocket();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void pre() {
		byte[] buf = "reset".getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length,
				resetAddress);
		try {
			resetSocket.send(packet);
		} catch (IOException e) {
			Runtime.getRuntime().runFinalization();
			System.exit(0);
		}
		sul.pre();
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
