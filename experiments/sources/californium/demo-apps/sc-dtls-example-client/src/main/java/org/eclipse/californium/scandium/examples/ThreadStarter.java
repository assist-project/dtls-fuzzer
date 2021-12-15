package org.eclipse.californium.scandium.examples;

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
public class ThreadStarter {
	
	private Supplier<ExampleDTLSClient> supplier;
	private ServerSocket srvSocket;
	private ExampleDTLSClient dtlsClientRunnable;
	private Thread dtlsClientThread;
	private Socket cmdSocket;
	private Integer port;
	private boolean continuous;
	private Integer runWait;
	
	public ThreadStarter(Supplier<ExampleDTLSClient> supplier, String ipPort, boolean continuous, Integer runWait) throws IOException {
		String[] addr = ipPort.split("\\:");
		port = Integer.valueOf(addr[1]);
		InetSocketAddress address = new InetSocketAddress(addr[0], port);		
		this.supplier = supplier;
		srvSocket = new ServerSocket();
		srvSocket.setReuseAddress(true);
		srvSocket.bind(address);
		this.continuous = continuous;
		this.runWait = runWait;
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ThreadStarter.this.closeAll();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}));
	}
	
	public void run() throws IOException {
		System.out.println("Listening at " + srvSocket.getInetAddress() + ":" + srvSocket.getLocalPort());
		do {
			cmdSocket = srvSocket.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(cmdSocket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cmdSocket.getOutputStream()));
			dtlsClientThread = null;
			while (true) {
				try {
					String cmd = in.readLine();
					System.out.println("Received: " + cmd);
					if (cmd != null) {
						switch(cmd.trim()) {
							// command for killing the current server thread and spawning a new one
						case "reset":
							// empty space acts as reset, used for debugging purposes
						case "":
							// we interrupt any existing server thread
							if (dtlsClientThread != null) {
								dtlsClientThread.interrupt();
								while (dtlsClientThread.isAlive()) {
									Thread.sleep(1);
								}
							}
							
							out.write("ack");
							out.newLine();
							out.flush();
							System.out.println("Waiting " + runWait + " ms");
							
							Thread.sleep(runWait);
							
							// spawn a new dtls server thread
							dtlsClientRunnable = supplier.get();
							dtlsClientThread = new Thread(dtlsClientRunnable);
							dtlsClientThread.start();
							
							// we wait for the server to launch, retrieve the port
							// and then, communicate the port to the other side
//							while (!dtlsClientThread.isRunning()) {
//								Thread.sleep(1);
//							}
//							out.write("ack");
//							out.newLine();
//							out.flush();
							break;
							
							// command for exiting
						case "exit":
							closeAll();
							return;
						}
					} else {
						System.out.println("Received Nothing");
						closeData();
						break;
					}
				} catch (Exception e) {
					String errorFileName = "ts.error." + port + ".log";
					PrintWriter errorPw = new PrintWriter(new FileWriter(errorFileName));
					e.printStackTrace(errorPw);
					e.printStackTrace();
					errorPw.close();
					closeAll();
					return;
				}
			}
		} while(continuous);
	}
	
	private void closeAll() throws IOException {
		System.out.println("Shutting down thread starter");
		closeData();
		srvSocket.close();
	}
	
	private void closeData() throws IOException{
		if (dtlsClientThread != null) {
			dtlsClientThread.interrupt();
		}
		if (cmdSocket != null) {
			cmdSocket.close();
		}
	}
}
