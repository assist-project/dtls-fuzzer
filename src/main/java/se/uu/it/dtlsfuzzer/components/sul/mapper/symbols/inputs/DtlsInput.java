package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.NotImplementedException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsState;

public abstract class DtlsInput extends TlsInput {

    /*
     * For the given message, modifies epoch and corresponding cipher to use for
     * encryption.
     */
    @XmlAttribute(name = "epoch", required = false)
    private Integer epoch = null;

    /*
     * Enable encryption for message (only applies if current epoch > 0).
     */
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

    public final void preSendUpdate(com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext context) {
        alias = getTlsExecutionContext(context).getSulDelegate().getFuzzingRole();
        TlsState state = getTlsExecutionContext(context).getState();

        // if different epoch than current, set the epoch in TLS context
        if (epoch != null && epoch != state.getTlsContext().getWriteEpoch()) {
            state.getTlsContext().setWriteEpoch(epoch);
            contextEpoch = state.getTlsContext().getWriteEpoch();
        }

        // if epoch > 0, deactivate encryption
        if (!encryptionEnabled) {
            throw new NotImplementedException("Disabling encryption is not currently supported.");
//            DeactivateEncryptionAction action = new DeactivateEncryptionAction();
//            action.setConnectionAlias(alias);
//            action.execute(state);
//            state.getTlsContext().setWriteSequenceNumber(context.incrementWriteRecordNumberEpoch0());
        }

        preSendDtlsUpdate(getTlsExecutionContext(context));
    }

    public void preSendDtlsUpdate(TlsExecutionContext context) {
    }

    public final void postSendUpdate(com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext context) {
        // reset epoch number and, if original epoch > 0, reactivate encryption
        if (contextEpoch != null) {
            getTlsExecutionContext(context).getState().getTlsContext().setWriteEpoch(contextEpoch);
            contextEpoch = null;
        }

        if (!encryptionEnabled) {
            throw new NotImplementedException("Re-enabling encryption is not currently supported.");
//            ActivateEncryptionAction action = new ActivateEncryptionAction();
//            action.setConnectionAlias(alias);
//            action.execute(state);
//            if (contextWriteNumber != null) {
//                state.getTlsContext().setWriteSequenceNumber(contextWriteNumber);
//            }
        }

        postSendDtlsUpdate(getTlsExecutionContext(context));
    }

    public void postSendDtlsUpdate(TlsExecutionContext context) {
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

    private long getWriteSequenceNumber(TlsContext context) {
        return context.getRecordLayer().getEncryptor().getRecordCipher(epoch).getState().getWriteSequenceNumber();
    }
}