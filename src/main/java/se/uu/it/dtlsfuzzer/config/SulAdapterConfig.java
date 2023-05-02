package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;

public class SulAdapterConfig {
    // In case a launch server is used to launch/terminate the SUT (as is done for JSSE and Scandium)
    @Parameter(names = "-adapterPort", required = false, description = "Port to which to send commands")
    private Integer adapterPort = null;

    @Parameter(names = "-adapterAddress", required = false, description = "Address to which to send commands")
    private String adapterAddress = "localhost";

    public Integer getAdapterPort() {
        return adapterPort;
    }

    public String getAdapterAddress() {
        return adapterAddress;
    }
}
