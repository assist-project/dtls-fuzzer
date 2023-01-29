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
// This could be made more general but...
public class ThreadStarter {
	public static final String CMD_RESET = "reset";
	public static final String CMD_EXIT = "exit";
	public static final String RESP_STOPPED = "stopped";
	
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
		dtlsThread = null;
		while (true) {
			try {
				String cmd = in.readLine();
				System.out.println("Received: " + cmd);
				if (cmd != null) {
					switch(cmd.trim()) {
					case CMD_RESET:
					case "":
						if (dtlsThread != null) {
							dtlsThread.interrupt();
							// waiting for the thread to die,
							// otherwise we might get address already in use problems
							while (dtlsThread.isAlive()) {
								Thread.sleep(1);
							}
						}
						if (config.isClient()) {
							//System.out.println("Writing " + String.valueOf(dtlsServerThread.getPort()));
							out.println("ack");
							out.flush();
							
							Thread.sleep(config.getRunWait());
							
							dtlsThread = newClientServer(config, sslContext, out);
							dtlsThread.start();
						} else {
							dtlsThread = newClientServer(config, sslContext, out);
							dtlsThread.start();
						
							// waiting for the server to start running
							while(!dtlsThread.isRunning()) {
								Thread.sleep(1);
							}
						
							out.println(String.valueOf(dtlsThread.getPort()));
							out.flush();
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

	private DtlsClientServer newClientServer(DtlsClientServerConfig config, SSLContext sslContext, PrintWriter out) {
		try {
			System.out.println("Creating a new server/client");
			DtlsClientServer clientServer = new DtlsClientServer(config, sslContext, new ThreadStarterEventListener(out));
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

		public void notifyStop() {
			writer.println(RESP_STOPPED);
			writer.flush();
		}
	}
}
