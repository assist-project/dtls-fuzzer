package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.CleanupTasks;
import se.uu.it.dtlsfuzzer.config.SulAdapterConfig;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class TlsSULAdapterWrapper implements SUL<TlsInput, TlsOutput>, DynamicPortProvider {

    private static final Logger LOGGER = LogManager.getLogger();

    private SUL<TlsInput, TlsOutput> sul;

    private TlsSULAdapter sulAdapter;

    private boolean server;

    /**
     *
     * @param sul
     * @param adapterConfig
     * @param server - are we acting as a server or client. It is assumed the launch server will launch the threads for the other role
     * @param tasks
     */
    public TlsSULAdapterWrapper(SUL<TlsInput, TlsOutput> sul, SulAdapterConfig adapterConfig,
            boolean server, CleanupTasks tasks) {
        this.sul = sul;
        this.server = server;
        this.sulAdapter = new TlsSULAdapter(adapterConfig, tasks);
        this.server = server;
    }

    public Integer getSULPort() {
        return sulAdapter.getSulPort();
    }

    @Override
    public void pre() {
        sulAdapter.connect();
        if (server) {
            // we are the server, the launch server is launching clients, we should start first (to get the ClientHello)
            sul.pre();
            sulAdapter.start();
        } else {
            // we are the client, the launch server is launching servers, we should launch a server first
            sulAdapter.start();
            sul.pre();
        }
    }

    @Override
    public void post() {
        sul.post();
        sulAdapter.stop();
    }

    @Override
    public TlsOutput step(TlsInput in) throws SULException {
        TlsOutput output = sul.step(in);
        if (sulAdapter.checkStopped()) {
            output.setAlive(false);
        }
        return output;
    }
}
