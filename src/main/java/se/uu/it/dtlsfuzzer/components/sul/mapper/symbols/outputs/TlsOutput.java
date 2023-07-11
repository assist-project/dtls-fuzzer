package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The outputs used in learning can be messages/records, application data, and
 * information about the state of the SUT (is it still alive).
 *
 * We restrict ourselves only to the received message types.
 */
public class TlsOutput extends AbstractOutput {
    // concrete list of messages
    private List<TlsMessage> messages;

    public TlsOutput(String name) {
        super(name);
        this.messages = Collections.emptyList();
    }

    public TlsOutput(String name, List<TlsMessage> messages) {
        super(name);
        this.messages = messages;
    }

    public static TlsOutput getSpecialOutput(String outString) {
        if (!specialOutputsMap.containsKey(outString))
            specialOutputsMap.put(outString, new TlsOutput(outString));
        return (TlsOutput) specialOutputsMap.get(outString);
    }

    /**
     * Special output abstractions separator d.
     */
    public static final String TIMEOUT = "TIMEOUT";
    public static final String UNKNOWN_MESSAGE = "UNKNOWN_MESSAGE";
    public static final String SOCKET_CLOSED = "SOCKET_CLOSED";
    public static final String DISABLED = "DISABLED";


    public static TlsOutput timeout() {
        return getSpecialOutput(TIMEOUT);
    }

    public static TlsOutput unknown() {
        return getSpecialOutput(UNKNOWN_MESSAGE);
    }

    public static TlsOutput socketClosed() {
        return getSpecialOutput(SOCKET_CLOSED);
    }

    public static TlsOutput disabled() {
        return getSpecialOutput(DISABLED);
    }


    /**
     * Only includes the abstract output
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        return builder.toString();
    }

    /**
     * Includes the output header and output details
     */
    public String toDetailedString() {
        // CLIENT_HELLO{isAlive=...}
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        String contentInfo = buildContentInfo();
        builder.append(contentInfo);
        return builder.toString();
    }


    /**
     * Indicates whether the output represents a record response from the system.
     * False means the output is describes timeout/crash/disabled-ness.
     */
    public boolean isRecordResponse() {
        return (messages != null && !messages.isEmpty()) || (!this.isTimeout() && !this.isDisabled());
    }

    /**
     * Indicates whether the output also contains the concrete messages from which the abstraction was derived
     */
    public boolean hasMessages() {
        return messages != null;
    }

    public List<TlsOutput> getTlsAtomicOutputs() {
        return getTlsAtomicOutputs(1);
    }

    public List<TlsOutput> getTlsAtomicOutputs(int unrollRepeating) {
        if (isAtomic() && !isRepeating()) {
            return Collections.singletonList(this);
        } else {
            List<TlsOutput> outputs = new LinkedList<>();
            for (String absOutput : getAtomicAbstractionStrings(unrollRepeating)) {
                TlsOutput output = new TlsOutput(absOutput);
                outputs.add(output);
            }
            return outputs;
        }
    }

    public List<String> getAtomicAbstractionStrings() {
        return getAtomicAbstractionStrings(1);
    }

    public String buildContentInfo() {
        StringBuilder builder = new StringBuilder();

        LinkedHashMap<String, String> printMap = new LinkedHashMap<>();
        printMap.put("isAlive", Boolean.toString(isAlive()));
        if (messages != null && !messages.isEmpty()) {
            printMap.put("messages", messages.toString());
        }

        builder.append("{");
        for (Map.Entry<String, String> entry : printMap.entrySet()) {
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
            builder.append(";");
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * Returns the protocol messages associated with the output.
     * Returns null if this output was generated from a specification and does not contain protocol messages.
     */
    public List<TlsMessage> getTlsMessages() {
        return messages;
    }
}
