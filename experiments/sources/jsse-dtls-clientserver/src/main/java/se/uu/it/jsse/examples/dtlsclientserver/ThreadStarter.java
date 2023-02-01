package se.uu.it.jsse.examples.dtlsclientserver;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLContext;

/**
 * We use this class to avoid having to restart the vm (which is can be a slow process). 
 */
public class ThreadStarter {
	public static final String CMD_RESET = "reset";
	public static final String CMD_EXIT = "exit";
	public static final String CMD_START = "start";
	public static final String CMD_STOP = "stop";
	public static final String RESP_STOPPED = "stopped";
	public static final String RESP_STARTED = "started";
	
	private ServerSocket srvSocket;
	private DtlsClientServer dtlsThread;
	private Socket cmdSocket;
	private Integer port;
	private DtlsClientServerConfig config;
	private SSLContext sslContext;

	public ThreadStarter(DtlsClientServerConfig config, SSLContext sslContext) throws IOException {
		String[] args = config.getThreadStarterIpPort().split("\\:");
		this.sslContext = sslContext;
		port = Integer.valueOf(args[1]);
		InetSocketAddress address = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
		srvSocket = new ServerSocket();
		srvSocket.setReuseAddress(true);
		srvSocket.setSoTimeout(20000);
		srvSocket.bind(address);
		this.config = config;
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ThreadStarter.this.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}));
	}
	
	public void run() throws IOException {
		System.out.println("Listening at " + srvSocket.getInetAddress() + ":" + srvSocket.getLocalPort());
		cmdSocket = srvSocket.accept();
		BufferedReader in = new BufferedReader(new InputStreamReader(cmdSocket.getInputStream()));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(cmdSocket.getOutputStream()));
		// Async notification of when the DTLS program starts/stops.
		EventListener listener = new ThreadStarterEventListener(out);
		dtlsThread = null;
		while (true) {
			try {
				String cmd = in.readLine();
				System.out.println("Received: " + cmd);
				if (cmd != null) {
					switch(cmd.trim()) {
					case CMD_START:
						if (dtlsThread != null && dtlsThread.isRunning()) {
							// even if the program is up and running, we still send STARTED.
							listener.notifyStart();
						} else {
							dtlsThread = newClientServer(config, sslContext, listener);
							dtlsThread.start();
							dtlsThread.isRunning();
						}
						break;
					case CMD_STOP:
						if (dtlsThread != null && dtlsThread.isRunning()) {
							dtlsThread.interrupt();
						} else {
							// even if the program is down, we still send STOPPED.
							listener.notifyStop();
						}
						break;
					case CMD_EXIT:
						close();
						return;
					}
				} else {
					close();
					return;
				}
			} catch (Exception e) {
				logException(e);
				close();
				return;
			}
		}
	}

	void logException(Exception e) throws IOException{
		String errorFileName = "ts.error." + port + ".log";
		PrintWriter errorPw = new PrintWriter(new FileWriter(errorFileName));
		e.printStackTrace(errorPw);
		errorPw.close();
	}
	
	void close() throws IOException {
		System.out.println("Sutting down thread starter");
		if (dtlsThread != null) {
			dtlsThread.interrupt();
		}
		if (cmdSocket != null) {
			cmdSocket.close();
		}
		srvSocket.close();
	}

	private DtlsClientServer newClientServer(DtlsClientServerConfig config, SSLContext sslContext, EventListener listener) {
		try {
			System.out.println("Creating a new server/client");
			DtlsClientServer clientServer = new DtlsClientServer(config, sslContext, listener);
			return clientServer;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	class ThreadStarterEventListener implements EventListener {
		private PrintWriter writer;

		ThreadStarterEventListener(PrintWriter writer) {
			this.writer = writer;
		}
		
		public void notifyStart() {
			writer.println(RESP_STARTED + " " + dtlsThread.getPort());
			writer.flush();
		}

		public void notifyStop() {
			writer.println(RESP_STOPPED);
			writer.flush();
		}
	}
}
