package se.uu.it.dtlsfuzzer.sut.input;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.action.ActivateEncryptionAction;
import de.rub.nds.tlsattacker.core.workflow.action.DeactivateEncryptionAction;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

public abstract class DtlsInput extends TlsInput {
	
	@XmlAttribute(name = "epoch", required = false)
	private Integer epoch = null;

	@XmlAttribute(name = "encryptedEnabled", required = false)
	private boolean encryptionEnabled = true;

	private String alias = null;
	private Integer contextEpoch = null;
	private Long contextWriteNumber = null;
	
	protected DtlsInput() {
		super();
	}

	protected DtlsInput(String name) {
		super(name);
	}
	
	public final void preSendUpdate(State state, ExecutionContext context) {
		alias = context.getSulDelegate().getRole();
		// if different epoch than current, set the epoch in TLS context
		if (epoch != null && epoch != state.getTlsContext().getDtlsSendEpoch()) {
			contextEpoch = state.getTlsContext().getDtlsSendEpoch();
			state.getTlsContext().setDtlsSendEpoch(epoch);
			// if epoch > 0, must deactivate encryption
			if (contextEpoch > 0  && !encryptionEnabled) {
				DeactivateEncryptionAction action = new DeactivateEncryptionAction();
				action.setConnectionAlias(alias);
				action.execute(state);
				contextWriteNumber = state.getTlsContext().getWriteSequenceNumber();
				state.getTlsContext().setWriteSequenceNumber(context.incrementWriteRecordNumberEpoch0());
			}
		}
		
		preSendDtlsUpdate(state, context);
	}
	
	public void preSendDtlsUpdate(State state, ExecutionContext context) {
	}

	public final void postSendUpdate(State state, ExecutionContext context) {
		// reset epoch number and, if original epoch > 0, reactivate encryption
		if (contextEpoch != null) {
			state.getTlsContext().setDtlsSendEpoch(contextEpoch);
			if (contextEpoch > 0  && !encryptionEnabled) {
				ActivateEncryptionAction action = new ActivateEncryptionAction();
				action.setConnectionAlias(alias);
				action.execute(state);
				if (contextWriteNumber != null) {
					state.getTlsContext().setWriteSequenceNumber(contextWriteNumber);
				}
			}
			contextEpoch = null;
		}
		
		postSendDtlsUpdate(state, context);
	}
	
	public void postSendDtlsUpdate(State state, ExecutionContext context) {
	}

	
	public Integer getEpoch() {
		return epoch;
	}
	
	public void setEpoch(Integer epoch) {
		this.epoch = epoch;
	}

	public boolean isEncryptionEnabled() {
		return encryptionEnabled;
	}

	public void setEncryptionEnabled(boolean encrypted) {
		this.encryptionEnabled = encrypted;
	}
}
