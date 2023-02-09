package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;
import java.util.List;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

/**
 * Configures how actual messages are mapped to abstract output strings.
 */
public class MapperConfig {
    @Parameter(names = "-repeatingOutputs", required = false, description = "Single or repeated occurrences of these outputs are mapped to a single repeating output (e.g. CLIENT_HELLO is mapped to CLIENT_HELLO "
            + TlsOutput.REPEATING_INDICATOR + " ).  "
            + "Used for outputs that the SUL may repeat an arbitrary number of times which may cause non-determinism. ")
    private List<String> repeatingOutputs = null;

    @Parameter(names = "-socketClosedAsTimeout", required = false, description = "Uses " + TlsOutput.TIMEOUT
            + " instead of " + TlsOutput.SOCKET_CLOSED + " outputs to identify when the system process is dead. "
            + "Useful for preventing non-determinism due to the arbitrary duration from when the system stops responding to when its process eventually dies. ")
    private boolean socketClosedAsTimeout = false;

    @Parameter(names = "-disabledAsTimeout", required = false, description = "Uses " + TlsOutput.TIMEOUT
            + " instead of " + TlsOutput.DISABLED)
    private boolean disabledAsTimeout = false;

    @Parameter(names = "-dontMergeRepeating", required = false, description = "Disables the merger of repeated outputs. "
            + "By default the mapper merges outputs which are repeated in succession (e.g. CLIENT_HELLO,CLIENT_HELLO) "
            + "into a single output to which '" + TlsOutput.REPEATING_INDICATOR + "' is postpended (CLIENT_HELLO"
            + TlsOutput.REPEATING_INDICATOR + ")")
    private boolean dontMergeRepeating = false;

    @Parameter(names = "-tlsAttackerReceiver", required = false, description = "Uses TlsAttacker's ReceiveMessageHelper to receive messages, instead of DTLS-Fuzzer's custom one ")
    private boolean tlsAttackerReceiver = false;

    public List<String> getRepeatingOutputs() {
        return repeatingOutputs;
    }

    public boolean isSocketClosedAsTimeout() {
        return socketClosedAsTimeout;
    }

    public boolean isDisabledAsTimeout() {
        return disabledAsTimeout;
    }

    public boolean isMergeRepeating() {
        return !dontMergeRepeating;
    }

    public boolean isTlsAttackerReceiver() {
        return tlsAttackerReceiver;
    }
}
