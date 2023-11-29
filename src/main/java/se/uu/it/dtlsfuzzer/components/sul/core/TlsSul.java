/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.uu.it.dtlsfuzzer.components.sul.core;


import java.io.IOException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulAdapter;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers.OutputMapper;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.InboundConnection;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.transport.TransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ClientUdpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ServerUdpTransportHandler;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.FileOutputStream;
import se.uu.it.dtlsfuzzer.components.sul.core.config.ConfigDelegate;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulClientConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsOutputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

/**
 * Implementation of {@link AbstractSul} that works for both clients and servers.
 * @author robert, paul
 */
public class TlsSul extends AbstractSul {

    private static final Logger LOGGER = LogManager.getLogger();
    private static Config config;

    private State state = null;
    private TlsExecutionContext context = null;

    /**
     * the Sul is closed if it has crashed resulting in IMCP packets, or it simply
     * terminated the connection
     */
    private boolean closed = false;

    /**
     * Are we imitating a server or a client instance.
     */
    private boolean server;


    private long resetWait = 0;
    private int count = 0;

    private SulConfig sulConfig;
    private Mapper defaultExecutor;
    private DynamicPortProvider portProvider;

    /**
     * Reference to thread waiting for a ClientHello to be received from the client.
     */
    private Thread chWaiter;

    /**
     * Have we received a ClientHello in the current run?
     */
    private boolean receivedClientHello;
    private OutputMapper outputMapper;

    private ConfigDelegate configDelegate;

    public TlsSul(TlsSulConfig sulConfig, MapperConfig mapperConfig, Mapper defaultExecutor,
            CleanupTasks cleanupTasks) {
        super(sulConfig, cleanupTasks);
        this.sulConfig = sulConfig;
        configDelegate = sulConfig.getConfigDelegate();
        this.defaultExecutor = defaultExecutor;
        server = sulConfig.isFuzzingClient();
        outputMapper = new DtlsOutputMapper(mapperConfig);
        if (server) {
            cleanupTasks.submit(new Runnable() {
                @Override
                public void run() {
                    if (state != null && chWaiter != null && chWaiter.isAlive()) {
                        try {
                            LOGGER.debug(
                                    "Causing existing ClientHello waiter thread to terminate by closing the connection.");
                            state.getTlsContext().getTransportHandler().closeConnection();
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
    public void setDynamicPortProvider(DynamicPortProvider portProvider) {
        this.portProvider = portProvider;
    }

    @Override
    public void pre() {
        Config config = getNewSulConfig(configDelegate);
        state = new State(config, new WorkflowTrace());
        TransportHandler transportHandler = null;

        if (configDelegate.getProtocolVersion().isDTLS()) {
            if (!server) {
                OutboundConnection connection = state.getConfig().getDefaultClientConnection();
                if (portProvider != null) {
                    connection.setPort(portProvider.getSulPort());
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

        if (server) {
            chWaiter = new Thread(new Runnable() {
                @Override
                public void run() {
                    initializeTransportHandler();
                }

            });
            chWaiter.start();
            receivedClientHello = false;
            if (((TlsSulClientConfig) sulConfig).getClientWait() > 0) {
                try {
                    Thread.sleep(((TlsSulClientConfig) sulConfig).getClientWait());
                } catch (InterruptedException e) {
                    LOGGER.error("Could not sleep thread");
                }
            }
        } else {
            initializeTransportHandler();
        }

        closed = false;
        resetWait = sulConfig.getStartWait();
        context = new TlsExecutionContext(sulConfig, new TlsState(state));
        LOGGER.debug("Start {}", count++);
    }

    private void initializeTransportHandler() {
        try {
            LOGGER.debug("Initializing transport handler");
            TransportHandler transportHandler = state.getTlsContext().getTransportHandler();
            transportHandler.preInitialize();
            transportHandler.initialize();
        } catch (IOException e) {
            LOGGER.error("Could not initialize transport handler");
            LOGGER.error(e, null);
        }
    }

    @Override
    public void post() {
        try {
            if (server && !receivedClientHello) {
                receiveClientHello();
            }
            TransportHandler transportHandler = state.getTlsContext().getTransportHandler();
            if (transportHandler == null) {
                LOGGER.error("Transport handler is null");
            } else {
                transportHandler.closeConnection();
                if (resetWait > 0) {
                    Thread.sleep(resetWait);
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
    public AbstractOutput step(AbstractInput in) {
        return doStep((TlsInput) in);
    }

    private AbstractOutput doStep(TlsInput in) {
        context.addStepContext();
        Mapper executor = in.getPreferredMapper(sulConfig);
        if (executor == null) {
            executor = defaultExecutor;
        }

        if (server && !receivedClientHello) {
            receiveClientHello();
        }

        if (!context.isExecutionEnabled()) {
            return outputMapper.disabled();
        }

        AbstractOutput output = null;
        try {
            if (state == null) {
                throw new RuntimeException("TLS-Attacker state is not initialized");
            } else {
                TransportHandler transportHandler = state.getTlsContext().getTransportHandler();
                if (transportHandler == null || transportHandler.isClosed() || closed) {
                    closed = true;
                    return outputMapper.socketClosed();
                }
            }

            output = executeInput(in, executor);

            if (output.equals(AbstractOutput.disabled()) || context.getStepContext().isDisabled()) {
                // this should lead to a disabled sink state
                context.disableExecution();
            }

            if (state.getTlsContext().isReceivedTransportHandlerException()) {
                closed = true;
            }
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            closed = true;
            return outputMapper.socketClosed();
        }
    }

    private AbstractOutput executeInput(TlsInput in, Mapper executor) {
        LOGGER.debug("sent: {}", in.toString());
        state.getTlsContext().setTalkingConnectionEndType(state.getTlsContext().getChooser().getConnectionEndType());
        long originalTimeout = state.getTlsContext().getTransportHandler().getTimeout();
        if (in.getExtendedWait() != null) {
            state.getTlsContext().getTransportHandler().setTimeout(originalTimeout + in.getExtendedWait());
        }
        if (sulConfig.getInputResponseTimeout() != null && sulConfig.getInputResponseTimeout().containsKey(in.getName())) {
            state.getTlsContext().getTransportHandler().setTimeout(sulConfig.getInputResponseTimeout().get(in.getName()));
        }

        AbstractOutput output = executor.execute(in, context);

        LOGGER.debug("received: {}", output);
        state.getTlsContext().getTransportHandler().setTimeout(originalTimeout);
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
     * Exports the TLS-Attacker configuration file after relevant parameters have been parsed.
     */
    private void exportEffectiveSulConfig(Config config, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            JAXBContext ctx = JAXBContext.newInstance(Config.class);
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.marshal(config, fos);
        } catch(Exception e) {
            LOGGER.error("Could not export configuration file");
            e.printStackTrace();
        }
    }
}
