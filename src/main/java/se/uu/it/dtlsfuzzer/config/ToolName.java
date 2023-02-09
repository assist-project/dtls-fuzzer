package se.uu.it.dtlsfuzzer.config;

public enum ToolName {
    STATE_FUZZER_CLIENT("state-fuzzer-client"),
    STATE_FUZZER_SERVER("state-fuzzer-server");

    private final String name;

    ToolName(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}
