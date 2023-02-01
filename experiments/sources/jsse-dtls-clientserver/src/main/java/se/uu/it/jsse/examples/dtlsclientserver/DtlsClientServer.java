/* * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved. * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. * * This code is free software; you can redistribute it and/or modify it * under the terms of the GNU General Public License version 2 only, as * published by the Free Software Foundation. * * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * Adapted from: 
 * http://cr.openjdk.java.net/~asmotrak/8159416/webrev.08/test/javax/net/ssl/DTLS/DTLSOverDatagram.java.html
 * 
 * An up-to-date version of this code is at:
 * https://hg.openjdk.java.net/jdk/jdk/file/00ae3b739184/test/jdk/javax/net/ssl/DTLS/DTLSOverDatagram.java
 * 
 * Very useful is the option.
 * -Djavax.net.debug=all
 */
package se.uu.it.jsse.examples.dtlsclientserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

/**
 * A basic DTLS echo server built around JSSE's SSLEngine.
 */
public class DtlsClientServer extends Thread {

	private static int LOG_LEVEL = 1; // 0 no logging, 1 basic logging, 2 logging incl. method name
	{
		String level = System.getProperty("log.level");
		if (level != null)
			LOG_LEVEL = Integer.valueOf(level);
	}
	private static final int BUFFER_SIZE = 20240;
	static final int SOCKET_TIMEOUT = 20000;

	private InetSocketAddress peerAddr;
	private DatagramSocket socket;
	private DtlsClientServerConfig config;
	private SSLContext sslContext;
	private AtomicBoolean running;
	private String side;
	private EventListener listener;

	public DtlsClientServer(DtlsClientServerConfig config, SSLContext sslContext, EventListener listener) throws GeneralSecurityException, IOException {
		info("DtlsClientServer: isClient = " + config.isClient());
		InetSocketAddress address = new InetSocketAddress(config.getHostname(), config.getPort());
		if (config.isClient()) {
			socket = new DatagramSocket();
			socket.connect(address);
			side = "Client";
			peerAddr = new InetSocketAddress(config.getHostname(), config.getPort());
			info("Hostname is " + config.getHostname());
			info("Port is " + config.getPort());
			info("Peer address is " + peerAddr.toString());
		} else {
			socket = new DatagramSocket(address);
			side = "Server";
			info("Receiving at port " + socket.getLocalPort());
		}
		socket.setSoTimeout(SOCKET_TIMEOUT);
		socket.setReuseAddress(true);
		this.config = config;		
		this.sslContext = sslContext;
		this.running = new AtomicBoolean(false);
		this.listener = listener;
	}

	public DtlsClientServer(DtlsClientServerConfig config, SSLContext sslContext) throws GeneralSecurityException, IOException {
		this(config, sslContext, EventListener.getNopEventListener());
	}

	/*
	 * A mock DTLS echo server which uses SSLEngine.
	 */
	public void run() {
		try {
			// create SSLEngine
			SSLEngine engine = createSSLEngine(sslContext, config.isClient(), config);
			running.set(true);
			listener.notifyStart();
			ByteBuffer appData = null;
			doFullHandshake(engine, socket);
			switch (config.getOperation()) {
			case BASIC:
				// basic mode, nothing more needs to be done
				break;
			case ONE_ECHO:
				if (!isInterrupted() && !isEngineClosed(engine)) {
					if (engine.getHandshakeStatus() == HandshakeStatus.NOT_HANDSHAKING) {
						appData = receiveAppData(engine, socket);
		
						if (appData != null) {
							info("Received application data");
		
							// write server application data
							sendAppData(engine, socket, appData.duplicate(), peerAddr, side);
						}
					}
				}
				break;
			case FULL:
				while (!isInterrupted() && !isEngineClosed(engine)) {
					if (engine.getHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING) {
						doHandshakeStepCatchExceptions(engine, socket);
					} else {
						appData = receiveAppData(engine, socket);

						if (appData != null) {
							info("Received application data");

							// write server application data
							sendAppData(engine, socket, appData.duplicate(), peerAddr, side);
						}
					}
				}
			case FULL_SR:
				// read server application data
				while (!isInterrupted()) {
					// ok, the engine is closed, if resumption was enabled we create a new engine,
					// otherwise we exit.
					if (isEngineClosed(engine)) {
						if (config.isResumptionEnabled()) {
							engine = createSSLEngine(sslContext, false, config);
							doFullHandshake(engine, socket);
						} else
							break;
					} else {
						if (engine.getHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING) {
							doHandshakeStepCatchExceptions(engine, socket);
						} else {
							appData = receiveAppData(engine, socket);

							if (appData != null) {
								info("Received application data");

								// write server application data
								sendAppData(engine, socket, appData.duplicate(), peerAddr, side);
							}
						}
					}

				}
				break;
			}
		} catch (Exception E) {
			severe(E.getMessage());
			E.printStackTrace(System.err);
		} finally {
			info("DTLS program terminated");
			if (isInterrupted()) {
				info("Thread has been interrupted");
			}
			socket.close();
			socket.disconnect();
			running.set(false);
			listener.notifyStop();
		}
	}

	public boolean isRunning() {
		return running.get();
	}

	private static boolean isEngineClosed(SSLEngine engine) {
		return (engine.isOutboundDone() && engine.isInboundDone());
	}

	/*
	 * basic SSL Engine
	 */
	private SSLEngine createSSLEngine(SSLContext context, boolean isClient, DtlsClientServerConfig config) throws Exception {
		SSLEngine engine = context.createSSLEngine();
		engine.setUseClientMode(isClient);

		if (!isClient) {
			switch (config.getAuth()) {
			case WANTED:
				engine.setWantClientAuth(true);
				break;
			case NEEDED:
				engine.setNeedClientAuth(true);
				break;
			default:
				break;
			}
		}

		SSLParameters params = engine.getSSLParameters();
		params.setEnableRetransmissions(config.isEnableRetransmission());
		engine.setSSLParameters(params);

		return engine;
	}

	public Integer getPort() {
		return this.socket.getLocalPort();
	}

	public void interrupt() {
		this.socket.close();
		super.interrupt();
	}

	/*
	 * Executes a full handshake, may or may not succeed.
	 * 
	 * The code is messy, what is important is that we do everything the SSLEngine
	 * tells us. There are essentially 4 commands an SSLEngine can issue (associated
	 * with the HandshakeStatus): 1. unwrap - meaning the engine is expecting to
	 * receive network data. This data should be received and inputted to the engine.
	 * 2. wrap - meaning the engine has network data ready. This data should be
	 * gathered from the engine and sent. 3. execute task - meaning the engine
	 * requests execution of some tasks. We should just execute them. 4. finished
	 * handshaking - the engine is done with the current handshake. That might mean
	 * that the handshake was completed successfully. Either that or invalid
	 * messages rendered the engine unable to continue with the handshake.
	 * 
	 * In the latter case, we expect the engine to be in a closed state, hence we
	 * instantiate a new engine. If resumption is enabled, we use the same context
	 * 
	 * Wrap and unwrap operations return results which also should be considered.
	 * 
	 */
	private void doFullHandshake(SSLEngine engine, DatagramSocket socket) throws Exception {

		boolean isDone = false;
		engine.beginHandshake();
		while (!isDone && !isEngineClosed(engine) && !isInterrupted() && !socket.isClosed()) {
			isDone = doHandshakeStepCatchExceptions(engine, socket);
		}

		if (isDone) {
			SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
			info("Handshake finished, status is " + hs);

			if (engine.getHandshakeSession() != null) {
				throw new Exception("Handshake finished, but handshake session is not null");
			}

			SSLSession session = engine.getSession();
			if (session == null) {
				throw new Exception("Handshake finished, but session is null");
			}
			info("Negotiated protocol is " + session.getProtocol());
			info("Negotiated cipher suite is " + session.getCipherSuite());
			
			if (LOG_LEVEL>0) {
				try {
					info("Verified peer certificates are " + Arrays.asList(engine.getSession().getPeerCertificates()));
				} catch(SSLPeerUnverifiedException exception) {
					info("SSL peer unverified");
				}
			}


			// handshake status should be NOT_HANDSHAKING
			//
			// according to the spec,
			// SSLEngine.getHandshakeStatus() can't return FINISHED
			if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
				throw new Exception("Unexpected handshake status " + hs);
			}
		}
	}

	/*
	 * TODO: this has to be re-worked
	 * Returns true if the handshake is completed, false otherwise.
	 */
	private boolean doHandshakeStepCatchExceptions(SSLEngine engine, DatagramSocket socket) {
		try {
			return doHandshakeStep(engine, socket);
		} catch (Exception exc) {
			info("Exception while executing handshake step");
			info(exc.getMessage());
			return false;
		}
	}

	// returns true if the handshake operation is completed/engine is closed, and
	// false otherwise
	private boolean doHandshakeStep(SSLEngine engine, DatagramSocket socket) throws Exception {
		SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
		info("handshake status: " + hs);
		List<DatagramPacket> packets;
		switch (hs) {
		// SSLEngine is expecting network data from the outside
		case NEED_UNWRAP:
		case NEED_UNWRAP_AGAIN:
			info("expecting DTLS records");
			ByteBuffer iNet;
			ByteBuffer iApp;
			if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
				byte[] buf = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					info("waiting for a packet");
					receivePacket(packet, socket);
					info("received a packet of length = " + packet.getLength());
				} catch (SocketTimeoutException ste) {
					info("socket timed out");
					return false;
				}

				iNet = ByteBuffer.wrap(buf, 0, packet.getLength());
				iApp = ByteBuffer.allocate(BUFFER_SIZE);
			} else {
				iNet = ByteBuffer.allocate(0);
				iApp = ByteBuffer.allocate(BUFFER_SIZE);
			}
			
			do {
				SSLEngineResult r = engine.unwrap(iNet, iApp);
				SSLEngineResult.Status rs = r.getStatus();
				logResult("unwrap", r);
				switch (rs) {
				case OK:
					continue;
				case BUFFER_OVERFLOW:
				case BUFFER_UNDERFLOW:
					throw new Exception("Unexpected buffer error: " + rs);
				case CLOSED:
	//				engine.closeInbound();
					return true;
				default:
					throw new Exception("This branch should not be reachable");
				}
			} while(iNet.hasRemaining());
			break;

		// SSLEngine wants to send network data to the outside world
		case NEED_WRAP:
			info("preparing to send DTLS records");
			packets = new ArrayList<>();
			info("pper address is " + peerAddr.toString());
			produceHandshakePackets(engine, peerAddr, packets);

			for (DatagramPacket p : packets) {
				socket.send(p);
			}
			break;

		// SSLEngine wants some tasks to be executed.
		case NEED_TASK:
			runDelegatedTasks(engine);
			break;

		// SSLEngine has finished handshaking
		case NOT_HANDSHAKING:
			info("finished handshaking");
			return true;

		case FINISHED:
			throw new Exception("Unexpected status, SSLEngine.getHandshakeStatus() " + "shouldn't return FINISHED");
		}

		return false;
	}
	
	// deliver application data
	private void sendAppData(SSLEngine engine, DatagramSocket socket, ByteBuffer appData, SocketAddress peerAddr,
			String side) throws Exception {

		List<DatagramPacket> packets = produceApplicationPackets(engine, appData, peerAddr);
		appData.flip();
		info("sending " + packets.size() + " packets");
		for (DatagramPacket p : packets) {
			socket.send(p);
		}
	}

	private ByteBuffer receiveAppData(SSLEngine engine, DatagramSocket socket) throws Exception {
		while (!isInterrupted() && !engine.isInboundDone()) {
			byte[] buf = new byte[BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			info("waiting for a packet");
			try {
				receivePacket(packet, socket);
			} catch (Exception e) {
				severe(e.getMessage());
				return null;
			}

			info("received a packet of length " + packet.getLength());
			ByteBuffer netBuffer = ByteBuffer.wrap(buf, 0, packet.getLength());
			ByteBuffer recBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			SSLEngineResult rs = engine.unwrap(netBuffer, recBuffer);
			logResult("unwrap", rs);
			recBuffer.flip();
			if (recBuffer.remaining() != 0) {
				return recBuffer;
			}
			if (engine.getHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING) {
				return null;
			}
		}

		return null;
	}

	// receive packet and update peer address while you are at it
	private void receivePacket(DatagramPacket packet, DatagramSocket socket) throws IOException {
		socket.receive(packet);
		InetSocketAddress peerAddress = (InetSocketAddress) packet.getSocketAddress();
		if (peerAddr == null || !peerAddress.equals(peerAddr)) {
			peerAddr = (InetSocketAddress) packet.getSocketAddress();
			info("setting peer address to " + peerAddr);
		}
	}

	// produce handshake packets
	private void produceHandshakePackets(SSLEngine engine, SocketAddress socketAddr, List<DatagramPacket> packets)
			throws Exception {

		while (engine.getHandshakeStatus() == HandshakeStatus.NEED_WRAP) {

			ByteBuffer oNet = ByteBuffer.allocate(32768);
			ByteBuffer oApp = ByteBuffer.allocate(0);
			SSLEngineResult r = engine.wrap(oApp, oNet);
			oNet.flip();

			logResult("wrap", r);
			Status rs = r.getStatus();

			switch (rs) {
			case BUFFER_UNDERFLOW:
			case BUFFER_OVERFLOW:
				throw new Exception("Unexpected buffer error: " + rs);
			case OK:
				if (oNet.hasRemaining()) {
					byte[] ba = new byte[oNet.remaining()];
					oNet.get(ba);
					DatagramPacket packet = createHandshakePacket(ba, socketAddr);
					packets.add(packet);
				}
				break;
			default:
				throw new Exception("This branch should not be reachable " + rs);
			}

		}

		info("produced " + packets.size() + " packets");
	}

	// produce application packets
	private List<DatagramPacket> produceApplicationPackets(SSLEngine engine, ByteBuffer source,
			SocketAddress socketAddr) throws Exception {

		List<DatagramPacket> packets = new ArrayList<>();
		ByteBuffer appNet = ByteBuffer.allocate(32768);
		SSLEngineResult r = engine.wrap(source, appNet);
		appNet.flip();
		logResult("wrap", r);
		SSLEngineResult.Status rs = r.getStatus();
		switch (rs) {
		case BUFFER_OVERFLOW:
		case BUFFER_UNDERFLOW:
			throw new Exception("Unexpected buffer error: " + rs);
		case OK:
			if (appNet.hasRemaining()) {
				byte[] ba = new byte[appNet.remaining()];
				appNet.get(ba);
				DatagramPacket packet = new DatagramPacket(ba, ba.length, socketAddr);
				packets.add(packet);
			}
			break;
		case CLOSED:
			throw new Exception("SSLEngine has closed unexpectedly");
		default:
			throw new Exception("This branch should not be reachable " + rs);
		}

		return packets;
	}

	private void logResult(String operation, SSLEngineResult result) {
		info(operation + " result: " + result);
	}

	private DatagramPacket createHandshakePacket(byte[] ba, SocketAddress socketAddr) {
		return new DatagramPacket(ba, ba.length, socketAddr);
	}

	// run delegated tasks
	private void runDelegatedTasks(SSLEngine engine) throws Exception {
		Runnable runnable;
		while ((runnable = engine.getDelegatedTask()) != null) {
			runnable.run();
		}

		SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
		if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
			throw new Exception("handshake shouldn't need additional tasks");
		}
	}

	static void severe(String message) {
		log(System.err, message);
	}

	static void info(String message) {
		log(System.out, message);
	}

	static void log(PrintStream ps, String message) {
		if (LOG_LEVEL > 0) {
			if (LOG_LEVEL > 1) {
				String methodName = Arrays.stream(Thread.currentThread().getStackTrace()).skip(3)
						.filter(e -> !e.getMethodName().startsWith("log")).findFirst().get().getMethodName();
				ps.println(methodName + ": " + message);
			} else {
				ps.println(message);
			}
		}
	}
}
