/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulAdapter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.MapperComposer;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapper;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.InboundConnection;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.layer.impl.FirstCachedUdpLayer;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.transport.TransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ClientUdpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ServerUdpTransportHandler;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.components.sul.core.config.ConfigDelegate;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulClientConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

/**
 * Implementation of {@link AbstractSul} that works for both clients and servers.
 *
 * @author robert, paul
 */
public class TlsSul implements AbstractSul<TlsInput, TlsOutput, TlsExecutionContext> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Stores original TLS-Attacker config parsed from file.
     * It is cloned on each run.
     */
    private static Config config;

    /**
     * Configures cloned TLS-Attacker config based on user-supplied arguments.
     */
    private ConfigDelegate configDelegate;

    /**
     * Stores the DTLS execution context.
     */
    private TlsExecutionContext context = null;

    /**
     * Set if the SUL is observed to have terminated the connection (e.g., following a crash).
     */
    private boolean closed = false;

    /**
     * Counts the number of calls to pre() for logging purposes.
     */
    private int count = 0;

    /**
     * Reference to thread waiting for a ClientHello to be received from the client.
     */
    private Thread chWaiter;

    /**
     * Used to signal when an initial ClientHello is received from the client.
     */
    private boolean receivedClientHello;

    /**
     * The provided SulConfig
     */
    private final TlsSulConfig sulConfig;

    private CleanupTasks cleanupTasks;

    private SulAdapter sulAdapter;

    private DynamicPortProvider dynamicPortProvider;

    /**
     * MapperComposer containing both input and output mapper
     */
    private MapperComposer<TlsInput, TlsOutput, TlsProtocolMessage, TlsExecutionContext, TlsState> mapperComposer;

    private OutputMapper<TlsOutput, TlsProtocolMessage, TlsExecutionContext> outputMapper;

    public TlsSul(TlsSulConfig sulConfig, MapperConfig mapperConfig, MapperComposer<TlsInput, TlsOutput, TlsProtocolMessage, TlsExecutionContext, TlsState> mapperComposer,
            CleanupTasks cleanupTasks) {
        this.sulConfig = sulConfig;
        this.mapperComposer = mapperComposer;
        this.cleanupTasks = cleanupTasks;
        this.outputMapper = mapperComposer.getOutputMapper();

        configDelegate = sulConfig.getConfigDelegate();

        if (sulConfig.isFuzzingClient()) {
            cleanupTasks.submit(new Runnable() {
                @Override
                public void run() {
                    if (context != null && chWaiter != null && chWaiter.isAlive()) {
                        try {
                            LOGGER.debug(
                                    "Causing existing ClientHello waiter thread to terminate by closing the connection.");
                            context.getTlsContext().getTransportHandler().closeConnection();
                        } catch (IOException e) {
                            LOGGER.error("IOException in TlsSul.run()");
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    void setSulAdapter(SulAdapter sulAdapter) {
        this.sulAdapter = sulAdapter;
    }

    @Override
    public void pre() {
        Config config = getNewSulConfig(configDelegate);
        config.getDefaultClientConnection().setUseIpv6(false); // fix NullPointerException
        config.getDefaultServerConnection().setUseIpv6(false); // fix NullPointerException
        State state = new State(config, new WorkflowTrace());
        context = new TlsExecutionContext(sulConfig, new TlsState(state));
        TransportHandler transportHandler = null;

        if (configDelegate.getProtocolVersion().isDTLS()) {
            if (!sulConfig.isFuzzingClient()) {
                OutboundConnection connection = state.getConfig().getDefaultClientConnection();
                if (dynamicPortProvider != null) {
                    connection.setPort(dynamicPortProvider.getSulPort());
                }
                transportHandler = new ClientUdpTransportHandler(connection);
            } else {
                InboundConnection connection = state.getConfig().getDefaultServerConnection();
                transportHandler = new ServerUdpTransportHandler(connection);
            }
        } else {
            throw new NotImplementedException(String.format("%s not supported", configDelegate.getProtocolVersion()));
        }
        state.getTlsContext().setTransportHandler(transportHandler);

        if (sulConfig.isFuzzingClient()) {
            chWaiter = new Thread(new Runnable() {
                @Override
                public void run() {
                    initializeTransportHandler();
                }

            });
            chWaiter.start();
            receivedClientHello = false;
            long clientWait = ((TlsSulClientConfig) sulConfig).getClientWait();
            if (clientWait > 0) {
                try {
                    Thread.sleep(clientWait);
                } catch (InterruptedException e) {
                    LOGGER.error("Could not sleep thread");
                }
            }
        } else {
            initializeTransportHandler();
        }

        closed = false;
        LOGGER.debug("Start {}", count++);
    }

    private void initializeTransportHandler() {
        try {
            LOGGER.debug("Initializing transport handler");
            TransportHandler transportHandler = context.getTlsContext().getTransportHandler();
            transportHandler.preInitialize();
            transportHandler.initialize();
            if (sulConfig.isFuzzingClient()) {
                boolean receivedClientHello = false;
                while (!receivedClientHello) {
                    try {
                        var firstClientHello = transportHandler.fetchData();
                        receivedClientHello = true;
                        FirstCachedUdpLayer udpLayer = (FirstCachedUdpLayer) context.getTlsContext().getLayerStack().getLowestLayer();
                        udpLayer.setFirstClientHelo(firstClientHello);
                        udpLayer.isFuzzingClient = true;
                    } catch (SocketTimeoutException e) {
                        // try again
                    }
                }
                var firstClientHello = outputMapper.receiveOutput(context);
                if (!firstClientHello.getName().equals("CLIENT_HELLO")) {
                    LOGGER.fatal("The first Client Hello should have been received");
                }
//                context.addStepContext();
            }
        } catch (IOException e) {
            LOGGER.error("Could not initialize transport handler");
            LOGGER.error(e, null);
        }
    }

    @Override
    public void post() {
        try {
            if (sulConfig.isFuzzingClient() && !receivedClientHello) {
                receiveClientHello();
            }
            TransportHandler transportHandler = context.getTlsContext().getTransportHandler();
            if (transportHandler == null) {
                LOGGER.error("Transport handler is null");
            } else {
                transportHandler.closeConnection();
                long startWait = sulConfig.getStartWait();
                if (startWait > 0) {
                    Thread.sleep(startWait);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not close connections");
            LOGGER.error(e, null);
        } catch (InterruptedException e) {
            LOGGER.error("Could not sleep thread");
            LOGGER.error(e, null);
        }
    }

    /*
     * If we play the role of the server, we should only begin interaction once a
     * ClientHello message is received.
     */
    private void receiveClientHello() {
        if (chWaiter != null && chWaiter.isAlive()) {
            try {
                chWaiter.join();
                receivedClientHello = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("ClientHello waiter thread was inexplicably interrupted");
            }
        }
    }

    @Override
    public TlsOutput step(TlsInput in) {
        context.addStepContext();

        if (sulConfig.isFuzzingClient() && !receivedClientHello) {
            receiveClientHello();
        }

        if (!context.isExecutionEnabled()) {
            return outputMapper.disabled();
        }

        TlsOutput output = null;
        try {
            TransportHandler transportHandler = context.getTlsContext().getTransportHandler();
            if (transportHandler == null || transportHandler.isClosed() || closed) {
                closed = true;
                return outputMapper.socketClosed();
            }

            output = executeInput(in);

            if (mapperComposer.getOutputChecker().isDisabled(output) || context.getStepContext().isDisabled()) {
                // this should lead to a disabled sink state
                context.disableExecution();
            }

            if (context.getTlsContext().isReceivedTransportHandlerException()) {
                closed = true;
            }
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            closed = true;
            return outputMapper.socketClosed();
        }
    }

    private TlsOutput executeInput(TlsInput in) {
        LOGGER.debug("sent: {}", in.toString());
        context.getTlsContext().setTalkingConnectionEndType(context.getTlsContext().getChooser().getConnectionEndType());
        long originalTimeout = context.getTlsContext().getTransportHandler().getTimeout();
        if (in.getExtendedWait() != null) {
            context.getTlsContext().getTransportHandler().setTimeout(originalTimeout + in.getExtendedWait());
        }
        if (sulConfig.getInputResponseTimeout() != null && sulConfig.getInputResponseTimeout().containsKey(in.getName())) {
            context.getTlsContext().getTransportHandler().setTimeout(sulConfig.getInputResponseTimeout().get(in.getName()));
        }

        TlsOutput output = mapperComposer.execute(in, context);

        LOGGER.debug("received: {}", output);
        context.getTlsContext().getTransportHandler().setTimeout(originalTimeout);
        return output;
    }

    private Config getNewSulConfig(ConfigDelegate delegate) {
        if (config == null) {
            try {
                config = Config.createConfig(delegate.getConfigInputStream());
                delegate.applyDelegate(config);
                if (delegate.getExportEffectiveSulConfig() != null) {
                    exportEffectiveSulConfig(config, delegate.getExportEffectiveSulConfig());
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not load configuration file");
            }
        }
        return config.createCopy();
    }

    /*
     * Exports the TLS-Attacker configuration after relevant parameters have been parsed.
     */
    private void exportEffectiveSulConfig(Config config, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            JAXBContext ctx = JAXBContext.newInstance(Config.class);
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.marshal(config, fos);
        } catch (Exception e) {
            LOGGER.error("Could not export configuration file");
            e.printStackTrace();
        }
    }

    @Override
    public SulConfig getSulConfig() {
        return sulConfig;
    }

    @Override
    public CleanupTasks getCleanupTasks() {
        return cleanupTasks;
    }

    @Override
    public void setDynamicPortProvider(DynamicPortProvider dynamicPortProvider) {
        this.dynamicPortProvider = dynamicPortProvider;
    }

    @Override
    public DynamicPortProvider getDynamicPortProvider() {
        return dynamicPortProvider;
    }

    @Override
    public Mapper<TlsInput, TlsOutput, TlsExecutionContext> getMapper() {
        return this.mapperComposer;
        }

    @Override
    public SulAdapter getSulAdapter() {
        return sulAdapter;
    }
}
