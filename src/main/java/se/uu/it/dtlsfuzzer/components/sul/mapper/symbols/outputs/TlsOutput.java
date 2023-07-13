package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.protocol.ProtocolMessage;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import java.util.Collections;
import java.util.List;

/**
 * The outputs used in learning can be messages/records, application data, and
 * information about the state of the SUT (is it still alive).
 *
 * We restrict ourselves only to the received message types.
 */
public class TlsOutput extends AbstractOutput {

    public TlsOutput(String name) {
        super(name);
        this.messages = Collections.emptyList();
    }

    public TlsOutput(String name, List<ProtocolMessage> messages) {
        super(name, Collections.emptyList());
    }
}
