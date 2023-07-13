package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class DtlsInputWrapper extends DtlsInput{
    private DtlsInput input;


    public DtlsInputWrapper(DtlsInput input) {
        this.input = input;
    }
    @Override
    public TlsInputType getInputType() {
        return input.getInputType();
    }

    public DtlsInput getInput() {
        return input;
    }


    public void setEpoch(Integer epoch) {
        input.setEpoch(epoch);
    }

    public Integer getEpoch() {
        return input.getEpoch();
    }

    public void setEncryptionEnabled(boolean encrypted) {
        input.setEncryptionEnabled(encrypted);
    }

    public boolean isEncryptionEnabled() {
        return input.isEncryptionEnabled();
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        return input.generateProtocolMessage(context);
    }

    public void preSendDtlsUpdate(TlsExecutionContext context) {
        input.preSendDtlsUpdate(context);
    }

    public void postSendDtlsUpdate(TlsExecutionContext context) {
        input.postSendDtlsUpdate(context);
    }

    public void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
            ExecutionContext context) {
        input.postReceiveUpdate(output, abstractOutputChecker, context);
    }
}
