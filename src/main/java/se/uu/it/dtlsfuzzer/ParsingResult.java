package se.uu.it.dtlsfuzzer;

import com.beust.jcommander.JCommander;
import se.uu.it.dtlsfuzzer.config.StateFuzzerClientConfig;
import se.uu.it.dtlsfuzzer.config.StateFuzzerConfig;
import se.uu.it.dtlsfuzzer.config.StateFuzzerServerConfig;

public class ParsingResult {
    private StateFuzzerClientConfig stateFuzzerClientConfig;
    private StateFuzzerServerConfig stateFuzzerServerConfig;
    private JCommander commander;

    public ParsingResult(StateFuzzerClientConfig stateFuzzerClientConfig, StateFuzzerServerConfig stateFuzzerServerConfig,
            JCommander commander) {
        this.stateFuzzerClientConfig = stateFuzzerClientConfig;
        this.stateFuzzerServerConfig = stateFuzzerServerConfig;
        this.commander = commander;
    }

    public JCommander getCommander() {
        return commander;
    }

    public StateFuzzerConfig getParsedConfig() {
        if (commander.getParsedCommand() != null) {
            if (commander.getParsedCommand().equals(stateFuzzerClientConfig.getToolName().getName())) {
                return stateFuzzerClientConfig;
            } else if (commander.getParsedCommand().equals(stateFuzzerServerConfig.getToolName().getName())) {
                return stateFuzzerServerConfig;
            }
        }
        return null;
    }

}
