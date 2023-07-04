/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.uu.it.dtlsfuzzer.sut;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.InboundConnection;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.record.layer.TlsRecordLayer;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.transport.TransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ClientUdpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ServerUdpTransportHandler;
import java.io.IOException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.config.ConfigDelegate;
import se.uu.it.dtlsfuzzer.config.DtlsSulClientConfig;
import se.uu.it.dtlsfuzzer.mapper.AbstractMapper;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;
import se.uu.it.dtlsfuzzer.mapper.Mapper;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.OutputMapper;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

/**
 * Note: SUT = System Under Test SUL = System Under Learning
 *
 * @author robert, paul
 */
public class TlsSUL extends AbstractSul {

    private static final Logger LOGGER = LogManager.getLogger();

    private State state = null;
    private ExecutionContext context = null;

    private Config config;

    /**
     * the SUT is closed if it has crashed resulting in IMCP packets, or it simply
     * terminated the connection
     */
    private boolean closed = false;

    /**
     * the sut is disabled if an input has disabled it as a result of a learning
     * purpose
     */
    private boolean disabled = false;

    /**
     * Are we imitating a server or a client instance.
     */
    private boolean server;

    private long resetWait = 0;

    private int count = 0;

    private SulConfig delegate;
    private AbstractMapper defaultExecutor;
    private String role;
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
    private MapperConfig mapperConfig;

    private ConfigDelegate configDelegate;

    public TlsSUL(SulConfig delegate, ConfigDelegate configDelegate, MapperConfig mapperConfig, AbstractMapper defaultExecutor,
            CleanupTasks cleanupTasks) {
        super(delegate, cleanupTasks);
        this.delegate = delegate;
        this.configDelegate = configDelegate;
        this.defaultExecutor = defaultExecutor;
        this.mapperConfig = mapperConfig;
        this.role = delegate.getFuzzingRole();
        server = delegate.isFuzzingClient();
        outputMapper = new OutputMapper(mapperConfig);
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
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void setDynamicPortProvider(DynamicPortProvider portProvider) {
        this.portProvider = portProvider;
    }

    @Override
    public void pre() {
        Config config = getNewSulConfig(configDelegate);
        configDelegate.applyDelegate(config);

        state = new State(config, new WorkflowTrace());
        config.setHighestProtocolVersion(configDelegate.getProtocolVersion());
        config.setDefaultSelectedProtocolVersion(configDelegate.getProtocolVersion());
        state.getTlsContext().setRecordLayer(new TlsRecordLayer(state.getTlsContext()));
        state.getTlsContext().setTransportHandler(null);

        if (configDelegate.getProtocolVersion().isDTLS()) {
            if (!server) {
                OutboundConnection connection = state.getConfig().getDefaultClientConnection();
                if (portProvider != null) {
                    connection.setPort(portProvider.getSulPort());
                }
                state.getTlsContext().setTransportHandler(new ClientUdpTransportHandler(connection));
            } else {
                InboundConnection connection = state.getConfig().getDefaultServerConnection();

                state.getTlsContext().setTransportHandler(new ServerUdpTransportHandler(connection));
            }
        } else {
            throw new NotImplementedException("TLS is not currently supported");
        }

        if (server) {
            chWaiter = new Thread(new Runnable() {
                @Override
                public void run() {
                    initializeTransportHandler();
                }

            });
            chWaiter.start();
            receivedClientHello = false;
            if (((DtlsSulClientConfig) delegate).getClientWait() > 0) {
                try {
                    Thread.sleep(((DtlsSulClientConfig) delegate).getClientWait());
                } catch (InterruptedException e) {
                    LOGGER.error("Could not sleep thread");
                }
            }
        } else {
            initializeTransportHandler();
        }

        closed = false;
        resetWait = delegate.getStartWait();
        context = new ExecutionContext(delegate, state);
        disabled = false;
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
            state.getTlsContext().getTransportHandler().closeConnection();
            if (resetWait > 0) {
                Thread.sleep(resetWait);
            }
        } catch (IOException e) {
            LOGGER.error("Could not close connections");
            LOGGER.error(e, null);
        } catch (InterruptedException e) {
            LOGGER.error("Could not sleep thread");
            LOGGER.error(e, null);
        } catch (NullPointerException e) {
            LOGGER.error("Transport handler is null");
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

    private TlsOutput doStep(TlsInput in) {
        context.addStepContext();
        Mapper executor = in.getPreferredMapper(delegate, mapperConfig);
        if (executor == null) {
            executor = defaultExecutor;
        }

        if (server && !receivedClientHello) {
            receiveClientHello();
        }

        if (!context.isExecutionEnabled()) {
            return outputMapper.disabled();
        }

        TlsOutput output = null;
        try {
            if (state == null) {
                throw new RuntimeException("TLS-Attacker state is not initialized");
            } else if (state.getTlsContext().getTransportHandler().isClosed() || closed) {
                closed = true;
                return outputMapper.socketClosed();
            }

            output = executeInput(in, executor, role);

            if (output == TlsOutput.disabled() || context.getStepContext().isDisabled()) {
                // this should lead to a disabled sink state
                context.disableExecution();
            }

            if (state.getTlsContext().isReceivedTransportHandlerException()) {
                closed = true;
            }
            return output;
        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
            closed = true;
            return outputMapper.socketClosed();
        }
    }

    private TlsOutput executeInput(TlsInput in, Mapper executor, String role) {
        LOGGER.debug("sent: {}", in.toString());
        state.getTlsContext().setTalkingConnectionEndType(state.getTlsContext().getChooser().getConnectionEndType());
        long originalTimeout = state.getTlsContext().getTransportHandler().getTimeout();
        if (in.getExtendedWait() != null) {
            state.getTlsContext().getTransportHandler().setTimeout(originalTimeout + in.getExtendedWait());
        }
        if (delegate.getInputResponseTimeout() != null && delegate.getInputResponseTimeout().containsKey(in.getName())) {
            state.getTlsContext().getTransportHandler().setTimeout(delegate.getInputResponseTimeout().get(in.getName()));
        }

        TlsOutput output = executor.execute(in, state, context);

        LOGGER.debug("received: {}", output);
        state.getTlsContext().getTransportHandler().setTimeout(originalTimeout);
        return output;
    }

    private Config getNewSulConfig(ConfigDelegate delegate) {
        if (config == null) {
            try {
                config = Config.createConfig(delegate.getConfigInputStream());
            } catch (IOException e) {
                throw new RuntimeException("Could not load configuration file");
            }
        }

        return config.createCopy();
    }

    public State getState() {
        return state;
    }
}
