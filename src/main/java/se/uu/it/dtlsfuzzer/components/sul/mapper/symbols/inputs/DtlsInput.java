package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import jakarta.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.NotImplementedException;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;

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

    private Integer contextEpoch = null;

    protected DtlsInput() {
        super();
    }

    protected DtlsInput(String name) {
        super(name);
    }

    @Override
    public final void preSendUpdate(TlsExecutionContext context) {
        // if different epoch than current, set the epoch in TLS context
        if (epoch != null && epoch != context.getTlsContext().getWriteEpoch()) {
            context.getTlsContext().setWriteEpoch(epoch);
            contextEpoch = context.getTlsContext().getWriteEpoch();
        }

        // if epoch > 0, deactivate encryption
        if (!encryptionEnabled) {
            throw new NotImplementedException("Disabling encryption is not currently supported.");
        }

        preSendDtlsUpdate(context);
    }

    public void preSendDtlsUpdate(TlsExecutionContext context) {
    }

    @Override
    public final void postSendUpdate(TlsExecutionContext context) {
        // reset epoch number and, if original epoch > 0, reactivate encryption
        if (contextEpoch != null) {
            context.getTlsContext().setWriteEpoch(contextEpoch);
            contextEpoch = null;
        }

        if (!encryptionEnabled) {
            throw new NotImplementedException("Re-enabling encryption is not currently supported.");
        }

        postSendDtlsUpdate(context);
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

}
