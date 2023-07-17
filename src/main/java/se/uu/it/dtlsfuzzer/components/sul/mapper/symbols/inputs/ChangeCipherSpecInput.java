package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.context.ExecutionContext;
import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import de.rub.nds.tlsattacker.core.record.cipher.CipherState;
import de.rub.nds.tlsattacker.core.record.cipher.RecordCipher;
import de.rub.nds.tlsattacker.core.record.cipher.RecordNullCipher;
import de.rub.nds.tlsattacker.core.record.crypto.Encryptor;
import de.rub.nds.tlsattacker.core.record.crypto.RecordCryptoUnit;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class ChangeCipherSpecInput extends DtlsInput {

    public ChangeCipherSpecInput() {
        super("CHANGE_CIPHER_SPEC");
    }

    @Override
    public void preSendDtlsUpdate(TlsExecutionContext context) {
        Encryptor encryptor = getState(context).getTlsContext().getRecordLayer().getEncryptor();
        int writeEpoch = getState(context).getTlsContext().getWriteEpoch();
        Long writeSeqNumForCurrentEpoch = encryptor.getRecordCipher(writeEpoch).getState().getWriteSequenceNumber();
        context.setWriteRecordNumberEpoch0(writeSeqNumForCurrentEpoch + 1);
    }

    public TlsProtocolMessage generateProtocolMessage(ExecutionContext context) {
        ChangeCipherSpecMessage ccs = new ChangeCipherSpecMessage(getState(context).getConfig());
        return new TlsProtocolMessage(ccs);
    }

    @Override
    public void postSendDtlsUpdate(TlsExecutionContext context) {
        // TLS-Attacker instantiates non-null ciphers even when the pre-master secret has not been yet negotiated.
        // Here, we replace the ciphers instantiated in such cases by null ciphers.
        // This ensures that encrypted messages are more likely to make sense to the SUT.
        if (getTlsContext(context).getPreMasterSecret() == null) {
            makeNullCipherAsMostRecent(getTlsContext(context).getRecordLayer().getEncryptor(), getTlsContext(context));
            makeNullCipherAsMostRecent(getTlsContext(context).getRecordLayer().getDecryptor(), getTlsContext(context));
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
