package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.record.cipher.CipherState;
import de.rub.nds.tlsattacker.core.record.cipher.RecordCipher;
import de.rub.nds.tlsattacker.core.record.cipher.RecordNullCipher;
import de.rub.nds.tlsattacker.core.record.crypto.RecordCryptoUnit;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.ExecutionContext;

public class ChangeCipherSpecInput extends DtlsInput {

    public ChangeCipherSpecInput() {
        super("CHANGE_CIPHER_SPEC");
    }

    @Override
    public void preSendDtlsUpdate(State state, ExecutionContext context) {
        Long writeSeqNumForCurrentEpoch = state.getTlsContext().getRecordLayer().getEncryptor().getRecordCipher(state.getTlsContext().getWriteEpoch()).getState().getWriteSequenceNumber();
        context.setWriteRecordNumberEpoch0(writeSeqNumForCurrentEpoch + 1);
    }

    public TlsMessage generateMessage(State state, ExecutionContext context) {
        ChangeCipherSpecMessage ccs = new ChangeCipherSpecMessage(
                state.getConfig());
        return ccs;
    }

    @Override
    public void postSendDtlsUpdate(State state, ExecutionContext context) {
        // TLS-Attacker 3.8.1 instantiates non-null ciphers even when the pre-master secret has not been yet negotiated.
        // Here, we replace the ciphers instantiated in such cases by null ciphers.
        // This ensures that encrypted messages are more likely to make sense to the SUT.
        if (state.getTlsContext().getPreMasterSecret() == null) {
            makeNullCipherAsMostRecent(state.getTlsContext().getRecordLayer().getEncryptor(), state.getTlsContext());
            makeNullCipherAsMostRecent(state.getTlsContext().getRecordLayer().getDecryptor(), state.getTlsContext());
        }
    }

    private void makeNullCipherAsMostRecent(RecordCryptoUnit cryptoUnit, TlsContext context) {
        RecordCipher cipher = cryptoUnit.getRecordMostRecentCipher();
        if (! (cipher instanceof RecordNullCipher)) {
            cryptoUnit.removeCiphers(1);
            CipherState cipherState = cipher.getState();
            cryptoUnit.addNewRecordCipher(new RecordNullCipher(context, cipherState));
        }
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.CCS;
    }

}
