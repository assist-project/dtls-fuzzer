package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

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
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
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
    public void postReceiveUpdate(TlsOutput output, OutputChecker<TlsOutput> abstractOutputChecker,
            TlsExecutionContext context) {
        input.postReceiveUpdate(output, abstractOutputChecker, context);
    }
}
