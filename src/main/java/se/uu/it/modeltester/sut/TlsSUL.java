/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.uu.it.modeltester.sut;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.record.layer.TlsRecordLayer;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.transport.tcp.ClientTcpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ClientUdpTransportHandler;
import se.uu.it.modeltester.config.SulDelegate;
import se.uu.it.modeltester.execute.AbstractInputExecutor;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.NonMutatingInputExecutor;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

/**
 *	Note: 
 *	SUT = System Under Test
 *	SUL = System Under Learning		
 *	
 * @author robert
 */
public class TlsSUL implements SUL<TlsInput, TlsOutput> {

    private static final Logger LOG = LogManager.getLogger();
    public static final String SUL_CONFIG = "/sul.config";
    private State state = null;
    private ExecutionContext context = null;
    
    private Config config;

    private boolean closed = false;
    
    private long resetWait = 0;
    
    private int count = 0;

    private SulDelegate delegate;
    
	private AbstractInputExecutor defaultExecutor;

    public TlsSUL(SulDelegate delegate, AbstractInputExecutor defaultExecutor) {
        this.delegate = delegate;
        this.defaultExecutor = defaultExecutor;
    }
    
    @Override
    public void pre() {
    	Config config = getSulConfig(delegate);
        delegate.applyDelegate(config);
        state = new State(config);
        state.getTlsContext().setRecordLayer(new TlsRecordLayer(state.getTlsContext()));
        state.getTlsContext().setTransportHandler(null);
    	config.setHighestProtocolVersion(delegate.getProtocolVersion());
    	config.setDefaultSelectedProtocolVersion(delegate.getProtocolVersion());
    	if (delegate.getProtocolVersion().isDTLS()) {
    		state.getTlsContext().setTransportHandler(new ClientUdpTransportHandler(state.getConfig().getDefaultClientConnection()));
    	} else {
    		state.getTlsContext().setTransportHandler(new ClientTcpTransportHandler(state.getConfig().getDefaultClientConnection()));
    	}
    	
        state.getTlsContext().initTransportHandler();
        state.getTlsContext().initRecordLayer();
        state.getTlsContext().setTalkingConnectionEndType(state.getTlsContext().getChooser().getConnectionEndType());
        closed = false;
        resetWait = delegate.getResetWait();
        context = new ExecutionContext();
        LOG.error("Start " + count++);
    }

    @Override
    public void post() {
        try {
            state.getTlsContext().getTransportHandler().closeConnection();
            if (resetWait > 0) {
            	Thread.sleep(resetWait);
            }
        } catch (IOException ex) {
            LOG.error("Could not close connections");
            LOG.error(ex, null);
        } catch (InterruptedException ex) {
        	LOG.error("Could not sleep thread");
            LOG.error(ex, null);
		} 
    }

    @Override
    public TlsOutput step(TlsInput in) throws SULException {
    	context.addStepContext();
    	AbstractInputExecutor executor = in.getExecutor();
    	if (executor == null) {
    		executor = defaultExecutor;
    	}
    	
    	TlsOutput output = null;
        try {
            if (state == null) {
                throw new RuntimeException("TLS-Attacker state is not initialized");
            } else if (state.getTlsContext().getTransportHandler().isClosed() || closed) {
                closed = true;
                return TlsOutput.socketClosed();
            }
            LOG.debug("sent:" + in.toString());
            output = executor.execute(in, state, context);
            LOG.debug("received:" + output);
            return output;
        } catch (IOException ex) {
            ex.printStackTrace();
            closed = true;
            return TlsOutput.socketClosed();
        }
    }
    
    private Config getSulConfig(SulDelegate delegate) {
		if (config == null) {
			if (delegate.getSulConfig() == null) {
				config = Config.createConfig(SulDelegate.class.getResourceAsStream(SUL_CONFIG));
				
			} else {
				config = Config.createConfig(new File(delegate.getSulConfig()));
			}
			delegate.applyDelegate(config);
		}
		
		return config;
	}
    
    public State getState() {
    	return state;
    }
}
