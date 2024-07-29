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
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.ralib.words.PSymbolInstance;
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
import java.io.IOException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.components.sul.core.config.ConfigDelegate;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulClientConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsMapperComposerRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsOutputMapperRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;

/**
 * Implementation of {@link AbstractSul} that works for both clients and
 * servers.
 *
 * @author robert, paul
 */
public class TlsSulRA implements AbstractSul<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> {

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
    private TlsExecutionContextRA context = null;

    /**
     * Set if the SUL is observed to have terminated the connection (e.g., following
     * a crash).
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
     * Output mapper used to generate special output symbols.
     */
    private DtlsOutputMapperRA outputMapper;

    private DtlsMapperComposerRA mapperComposer;

    private TlsSulConfig sulConfig;

    private CleanupTasks cleanupTasks;

    private SulAdapter sulAdapter;

    private DynamicPortProvider dynamicPortProvider;

    public TlsSulRA(TlsSulConfig sulConfig, MapperConfig mapperConfig,
            DtlsMapperComposerRA mapperComposer,
            CleanupTasks cleanupTasks) {
        this.sulConfig = sulConfig;
        this.cleanupTasks = cleanupTasks;
        this.mapperComposer = mapperComposer;

        outputMapper = new DtlsOutputMapperRA(mapperConfig, mapperComposer.getOutputBuilder());
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

    @Override
    public void pre() {
        Config config = getNewSulConfig(configDelegate);
        State state = new State(config, new WorkflowTrace());
        context = new TlsExecutionContextRA((TlsSulConfig) sulConfig, new TlsState(state));
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
        LOGGER.debug("Start {}", count++);
    }

    private void initializeTransportHandler() {
        try {
            LOGGER.debug("Initializing transport handler");
            TransportHandler transportHandler = context.getTlsContext().getTransportHandler();
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
            if (sulConfig.isFuzzingClient() && !receivedClientHello) {
                receiveClientHello();
            }
            TransportHandler transportHandler = context.getTlsContext().getTransportHandler();
            if (transportHandler == null) {
                LOGGER.error("Transport handler is null");
            } else {
                transportHandler.closeConnection();
                if (sulConfig.getStartWait() > 0) {
                    Thread.sleep(sulConfig.getStartWait());
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
    public PSymbolInstance step(PSymbolInstance in) {
        return doStep(in);
    }

    private PSymbolInstance doStep(PSymbolInstance in) {
        context.addStepContext();
        // Mapper<TlsInput, TlsOutput, TlsExecutionContext> mapper = this.getMapper();
        // if (mapper == null) { todo: dead store according to spotbugs, see if this is
        // needed.
        // mapper = this.mapper;
        // }

        if (sulConfig.isFuzzingClient() && !receivedClientHello) {
            receiveClientHello();
        }

        if (!context.isExecutionEnabled()) {
            return outputMapper.disabled();
        }

        PSymbolInstance output = null;
        try {
            TransportHandler transportHandler = context.getTlsContext().getTransportHandler();
            if (transportHandler == null || transportHandler.isClosed() || closed) {
                closed = true;
                return outputMapper.socketClosed();
            }

            output = executeInput(in);

            if (output.equals(mapperComposer.getOutputBuilder().buildDisabled())
                    || context.getStepContext().isDisabled()) {
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

    private PSymbolInstance executeInput(PSymbolInstance in) {
        LOGGER.debug("sent: {}", in.toString());
        context.getTlsContext()
                .setTalkingConnectionEndType(context.getTlsContext().getChooser().getConnectionEndType());
        long originalTimeout = context.getTlsContext().getTransportHandler().getTimeout();
        if (in.getExtendedWait() != null) {
            context.getTlsContext().getTransportHandler().setTimeout(originalTimeout + in.getExtendedWait());
        }
        if (sulConfig.getInputResponseTimeout() != null
                && sulConfig.getInputResponseTimeout().containsKey(in.getBaseSymbol().getName())) {
            context.getTlsContext().getTransportHandler()
                    .setTimeout(sulConfig.getInputResponseTimeout().get(in.getBaseSymbol().getName()));
        }

        PSymbolInstance output = mapperComposer.execute(in, context);

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
     * Exports the TLS-Attacker configuration file after relevant parameters have
     * been parsed.
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
        return this.sulConfig;
    }

    @Override
    public CleanupTasks getCleanupTasks() {
        return cleanupTasks;
    }

    @Override
    public DynamicPortProvider getDynamicPortProvider() {
        return dynamicPortProvider;
    }

    @Override
    public void setDynamicPortProvider(DynamicPortProvider dynamicPortProvider) {
        this.dynamicPortProvider = dynamicPortProvider;
    }

    @Override
    public Mapper<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> getMapper() {
        return this.mapperComposer;
    }

    @Override
    public SulAdapter getSulAdapter() {
        return this.sulAdapter;
    }

    void setSulAdapter(SulAdapter sulAdapter) {
        this.sulAdapter = sulAdapter;
    }
}
