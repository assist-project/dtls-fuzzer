/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.rub.nds.modelfuzzer.sut;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import de.rub.nds.modelfuzzer.config.SulDelegate;
import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.record.layer.TlsRecordLayer;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.transport.tcp.ClientTcpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ClientUdpTransportHandler;

/**
 *	Note: 
 *	SUT = System Under Test
 *	SUL = System Under Learning		
 *	
 * @author robert
 */
public class TlsSUL implements SUL<TlsInput, TlsOutput> {

    private static final Logger LOG = LogManager.getLogger();
    private State state = null;

    private Config config;

    private boolean closed = false;
    
    private long resetWait = 0;
    
    private int count = 0;

    private SulDelegate delegate;

    public TlsSUL(SulDelegate delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void pre() {
        config = Config.createConfig();
        config.setEnforceSettings(false);
        config.setQuickReceive(false);
        config.setEarlyStop(false);
        config.setStopActionsAfterFatal(false);
        config.setStopRecievingAfterFatal(false);
        config.setAddServerNameIndicationExtension(true);
        config.setAddRenegotiationInfoExtension(Boolean.TRUE);
        config.setAddSignatureAndHashAlgorithmsExtension(Boolean.TRUE);
        config.setAddHeartbeatExtension(true);
        delegate.applyDelegate(config);
        config.setDefaultSelectedCipherSuite(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
        config.setDefaultClientSupportedCiphersuites(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
        config.setDefaultServerSupportedCiphersuites(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
        config.setAddEllipticCurveExtension(false);
        config.setAddECPointFormatExtension(false);
        config.setUseFreshRandom(false);
//        state.getConfig().setDefaultSelectedCipherSuite(suite);
//        state.getConfig().setDefaultServerSupportedCiphersuites(suite);
//        state.getConfig().setDefaultClientSupportedCiphersuites(suite);
//        if (suite.name().contains("EC")) {
//            state.getConfig().setAddECPointFormatExtension(true);
//            state.getConfig().setAddEllipticCurveExtension(true);
//        } else {
//            state.getConfig().setAddECPointFormatExtension(false);
//            state.getConfig().setAddEllipticCurveExtension(false);
//        } if (suite.isPsk()) {
//        	state.getConfig().setDefaultPSKKey(new byte [] {0x12, 0x34});
//        	state.getConfig().setDefaultPSKIdentity("Client_identity".getBytes(Charset.forName("UTF-8")));
//        }
//        state.getConfig().setAddHeartbeatExtension(true);
        byte [] a = new byte[32];
        for (int i=0; i<a.length; i++)
        	a[i] = 1;
        config.setDefaultClientRandom(a);
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
        closed = false;
        resetWait = delegate.getResetWait();
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
    	InputExecutor executor = in.getExecutor();
    	TlsOutput output = null;
        try {
            if (state == null) {
                throw new RuntimeException("TLS-Attacker state is not initialized");
            } else if (state.getTlsContext().getTransportHandler().isClosed() || closed) {
                closed = true;
                return TlsOutput.socketClosed();
            }
            LOG.info("sent:" + in.toString());
            output = executor.execute(in, state);
            LOG.info("received:" + output);
            return output;
        } catch (IOException ex) {
            ex.printStackTrace();
            closed = true;
            return TlsOutput.socketClosed();
        }
    }
    
    public State getState() {
    	return state;
    }
}
