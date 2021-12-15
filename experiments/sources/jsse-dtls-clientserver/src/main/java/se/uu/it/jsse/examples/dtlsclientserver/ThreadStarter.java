package se.uu.it.jsse.examples.dtlsclientserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

/**
 * We use this class to avoid having to restart the vm (which is can be a slow process). 
 */
// This could be made more general but...
public class ThreadStarter {
	
	private Supplier<DtlsClientServer> supplier;
	private ServerSocket srvSocket;
	private DtlsClientServer dtlsServerThread;
	private Socket cmdSocket;
	private Integer port;
	private DtlsClientServerConfig config;

	public ThreadStarter(Supplier<DtlsClientServer> supplier, DtlsClientServerConfig config) throws IOException {
//		String[] args = ipPort.split("\\:");
		String[] args = config.getThreadStarterIpPort().split("\\:");
		port = Integer.valueOf(args[1]);
		InetSocketAddress address = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
		this.supplier = supplier;
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
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cmdSocket.getOutputStream()));
		dtlsServerThread = null;
		while (true) {
			try {
				String cmd = in.readLine();
				System.out.println("Received: " + cmd);
				if (cmd != null) {
					switch(cmd.trim()) {
					case "reset":
					case "":
						if (dtlsServerThread != null) {
							dtlsServerThread.interrupt();
							// waiting for the thread to die,
							// otherwise we might get address already in use problems
							while (dtlsServerThread.isAlive()) {
								Thread.sleep(1);
							}
						}
						if (config.isClient()) {
							//System.out.println("Writing " + String.valueOf(dtlsServerThread.getPort()));
							out.write("ack");
							out.newLine();
							out.flush();
							
							Thread.sleep(config.getRunWait());
							
							dtlsServerThread = supplier.get();
							dtlsServerThread.start();
						} else {
							dtlsServerThread = supplier.get();
							dtlsServerThread.start();
						
							// waiting for the server to start running
							while(!dtlsServerThread.isRunning()) {
								Thread.sleep(1);
							}
						
							out.write(String.valueOf(dtlsServerThread.getPort()));
							out.newLine();
							out.flush();
						}
						break;
					case "exit":
						close();
						return;
					}
				} else {
					close();
					return;
				}
			} catch (Exception e) {
				String errorFileName = "ts.error." + port + ".log";
				PrintWriter errorPw = new PrintWriter(new FileWriter(errorFileName));
				e.printStackTrace(errorPw);
				errorPw.close();
				close();
				return;
			}
		}
	}
	
	void close() throws IOException {
		System.out.println("Sutting down thread starter");
		if (dtlsServerThread != null) {
			dtlsServerThread.interrupt();
		}
		if (cmdSocket != null) {
			cmdSocket.close();
		}
		srvSocket.close();
	}
}
