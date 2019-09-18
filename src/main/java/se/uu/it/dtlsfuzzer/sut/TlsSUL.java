/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.uu.it.dtlsfuzzer.sut;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.PortableServer.THREAD_POLICY_ID;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.record.layer.TlsRecordLayer;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.transport.tcp.ClientTcpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ClientUdpTransportHandler;
import se.uu.it.dtlsfuzzer.config.SulDelegate;
import se.uu.it.dtlsfuzzer.execute.AbstractInputExecutor;
import se.uu.it.dtlsfuzzer.execute.ExecutionContext;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;

/**
 * Note: SUT = System Under Test SUL = System Under Learning
 *
 * @author robert, paul
 */
public class TlsSUL implements SUL<TlsInput, TlsOutput> {

	private static final Logger LOG = LogManager.getLogger();
	private State state = null;
	private ExecutionContext context = null;

	private Config config;

	/**
	 * the sut is closed if it has crashed resulting in IMCP packets, or it
	 * simply terminated the connection
	 */
	private boolean closed = false;

	/**
	 * the sut is disabled if an input has disabled it as a result of a learning
	 * purpose
	 */
	private boolean disabled = false;

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
		Config config = getNewSulConfig(delegate);
		config.setAutoSelectCertificate(false);
		delegate.applyDelegate(config);
		state = new State(config);
		state.getTlsContext().setRecordLayer(
				new TlsRecordLayer(state.getTlsContext()));
		state.getTlsContext().setTransportHandler(null);
		config.setHighestProtocolVersion(delegate.getProtocolVersion());
		config.setDefaultSelectedProtocolVersion(delegate.getProtocolVersion());
		if (delegate.getProtocolVersion().isDTLS()) {
			state.getTlsContext().setTransportHandler(
					new ClientUdpTransportHandler(state.getConfig()
							.getDefaultClientConnection()));
		} else {
			state.getTlsContext().setTransportHandler(
					new ClientTcpTransportHandler(state.getConfig()
							.getDefaultClientConnection()));
		}
		state.getTlsContext().initTransportHandler();
		state.getTlsContext().initRecordLayer();
		closed = false;
		resetWait = delegate.getResetWait();
		context = new ExecutionContext();
		disabled = false;
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
		AbstractInputExecutor executor = in.getPreferredExecutor();
		if (executor == null) {
			executor = defaultExecutor;
		}

		if (disabled) {
			return TlsOutput.disabled();
		}

		TlsOutput output = null;
		try {
			if (state == null) {
				throw new RuntimeException(
						"TLS-Attacker state is not initialized");
			} else if (state.getTlsContext().getTransportHandler().isClosed()
					|| closed) {
				closed = true;
				return TlsOutput.socketClosed();
			}

			output = executeInput(in, executor);

			if (output == TlsOutput.disabled()
					|| context.getStepContext().isDisabled()) {
				// this should lead to a disabled sink state
				disabled = true;
			}

			if (state.getTlsContext().isReceivedTransportHandlerException()) {
				closed = true;
			}
			return output;
		} catch (IOException ex) {
			ex.printStackTrace();
			closed = true;
			return TlsOutput.socketClosed();
		}
	}

	private TlsOutput executeInput(TlsInput in, AbstractInputExecutor executor) {
		LOG.debug("sent:" + in.toString());
		state.getTlsContext().setTalkingConnectionEndType(
				state.getTlsContext().getChooser().getConnectionEndType());
		long originalTimeout = state.getTlsContext().getTransportHandler()
				.getTimeout();
		if (in.getExtendedWait() != null)
			state.getTlsContext().getTransportHandler()
					.setTimeout(originalTimeout + in.getExtendedWait());
		TlsOutput output = executor.execute(in, state, context);
		LOG.debug("received:" + output);
		state.getTlsContext().getTransportHandler().setTimeout(originalTimeout);
		return output;
	}

	private Config getNewSulConfig(SulDelegate delegate) {
		if (config == null) {
			try {
				config = Config
						.createConfig(delegate.getSulConfigInputStream());
			} catch (IOException e) {
				throw new RuntimeException("Could not load configuration file");
			}
			delegate.applyDelegate(config);
		}

		return config.createCopy();
	}

	public State getState() {
		return state;
	}
}
