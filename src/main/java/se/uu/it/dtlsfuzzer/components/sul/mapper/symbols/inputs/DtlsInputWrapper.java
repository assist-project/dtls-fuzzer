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

    @Override
    public void setEpoch(Integer epoch) {
        input.setEpoch(epoch);
    }

    @Override
    public Integer getEpoch() {
        return input.getEpoch();
    }

    @Override
    public void setEncryptionEnabled(boolean encrypted) {
        input.setEncryptionEnabled(encrypted);
    }

    @Override
    public boolean isEncryptionEnabled() {
        return input.isEncryptionEnabled();
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        return input.generateProtocolMessage(context);
    }

    @Override
    public void preSendDtlsUpdate(TlsExecutionContext context) {
        input.preSendDtlsUpdate(context);
    }

    @Override
    public void postSendDtlsUpdate(TlsExecutionContext context) {
        input.postSendDtlsUpdate(context);
    }

    @Override
    public void postReceiveUpdate(AbstractOutput output, AbstractOutputChecker abstractOutputChecker,
            ExecutionContext context) {
        input.postReceiveUpdate(output, abstractOutputChecker, context);
    }
}
