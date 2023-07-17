package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulAdapter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulAdapterConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
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


/**
 * The adapter used to communicate with a DTLS SUL launch server responsible for starting/stopping the SUL.
 * It issues to the server the following commands:
 * <br/>
 * "start" - prompts the launch server to launch the SUL, and respond with "started ${port}" once the SUT is running.
 * Therein, ${port} is the local port of the SUL (useful for testing servers when this port is dynamically allocated).
 * <br/>
 * "stop" - prompts the launch server to stop the current SUL process.
 * The server generates "stopped" to signal that the SUL process has terminated.
 */
public class TlsSulAdapter implements SulAdapter {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CMD_STOP = "stop";
    private static final String CMD_START = "start";
    private static final String RESP_STOPPED = "stopped";
    private static final String RESP_STARTED = "started";

    private Socket adapterSocket;
    private InetSocketAddress resetAddress;
    private Integer sulPort;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean stopped;
    private boolean isClientLauncher;

    public TlsSulAdapter(SulAdapterConfig adapterConfig, CleanupTasks tasks, boolean isClientLauncher) {
        resetAddress = new InetSocketAddress(adapterConfig.getAdapterAddress(), adapterConfig.getAdapterPort());
        try {
            adapterSocket = new Socket();
            adapterSocket.setReuseAddress(true);
            adapterSocket.setSoTimeout(20000);
            tasks.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!adapterSocket.isClosed()) {
                            LOGGER.debug("Closing socket");
                            adapterSocket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        stopped = true;
        this.isClientLauncher = isClientLauncher;
    }

    /**
     * Connects to the SUL launch server if not already connected.
     */
    public void connect() {
        try {
            if (!adapterSocket.isConnected()) {
                adapterSocket.connect(resetAddress);
                reader = new BufferedReader(new InputStreamReader(adapterSocket.getInputStream()));
                writer = new PrintWriter(new OutputStreamWriter(adapterSocket.getOutputStream()), true);
            }
        } catch(IOException e) {
            throw new TlsSulAdapterException(e);
        }
    }

    /**
     * Asks the SUL launch server to launch a new SUL thread.
     */
    public void start() {
        if (!checkStopped()) {
            throw new TlsSulAdapterException("SUL still running");
        }
        writer.println(CMD_START);
        String resp = null;
        try {
            resp = reader.readLine();
        } catch (IOException e) {
            throw new TlsSulAdapterException(e);
        }
        String[] split = resp.split("\\ ");
        if (!split[0].equals(RESP_STARTED)) {
            throw new TlsSulAdapterException(String.format("Received invalid response to %s command", CMD_START));
        }
        String portString = split[1];
        if (portString == null) {
            throw new TlsSulAdapterException("Server has closed the socket");
        }
        stopped = false;
        sulPort = Integer.valueOf(portString);
    }


    /**
     * Asks the SUL launch server to terminate the current SUL thread (if the thread hasn't terminated already).
     */
    public void stop(){
        if (!checkStopped()) {
            writer.println(CMD_STOP);
            String response;
            try {
                response = reader.readLine();
                if (!response.equals(RESP_STOPPED)) {
                    throw new TlsSulAdapterException(String.format("Received invalid response to %s command", CMD_STOP));
                }
                stopped = true;
            } catch (IOException e) {
                throw new TlsSulAdapterException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean checkStopped() {
        // has the SUT stopped after executing the input?
        try {
            if (reader.ready()) {
                String response = reader.readLine();
                if (response.equals(RESP_STOPPED)) {
                    LOGGER.debug("Server stopped");
                    stopped = true;
                } else {
                    throw new TlsSulAdapterException("Received invalid response");
                }
            }
        } catch (IOException e) {
            throw new TlsSulAdapterException(e);
        }
        return stopped;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getSulPort() {
        return sulPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClientLauncher() {
        return isClientLauncher;
    }
}
