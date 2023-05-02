package se.uu.it.dtlsfuzzer.config;

import com.beust.jcommander.Parameter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import net.automatalib.words.Alphabet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.CleanupTasks;
import se.uu.it.dtlsfuzzer.sut.input.AlphabetFactory;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.input.xml.AlphabetSerializer;

public class TimingProbe {
    private static final Logger LOGGER = LogManager.getLogger(TimingProbe.class);

    public static void runTimingProbe(TimingProbeEnabler config) throws FileNotFoundException {
        CleanupTasks cleanupTasks = new CleanupTasks();
        Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
        TimingProbe probe = ((TimingProbeProvider) config).getTimingProbe();
        if (probe.isActive()) {
            ProbeTestRunner probeTestRunner = null;
            try {
                probeTestRunner = new ProbeTestRunner(config.getTestRunnerConfig(), alphabet, config.getSulDelegate(), config.getMapperConfig(), cleanupTasks);
                probe.setProbeTestRunner(probeTestRunner);
                if (probe.isValid()) {

                        Map<String, Integer> bestTimes = probe.findDeterministicTimesValues();

                        LOGGER.info(TimingProbe.present(bestTimes));
                        try {
                            probe.exportAlphabet();
                        } catch (IOException | JAXBException e) {
                            e.printStackTrace();
                        }
                }
                } catch (ProbeException e) {
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                } catch (IOException e1) {
                    LOGGER.error(e1.getMessage());
                    e1.printStackTrace();
                } finally {
                    if (probeTestRunner != null) {
                        probeTestRunner.terminate();
                    }
                }
        }
    }

    @Parameter(names = "-timingProbe", required = false, description = "Probe for timing values by testing for non-determinism")
    private String probeCmd = null;

    @Parameter(names = "-probeMin", required = false, description = "Minimum timing value for probe")
    private Integer probeMin = 10;

    @Parameter(names = "-probeLow", required = false, description = "Lowest timing value for probe")
    private Integer probeLo = 0;

    @Parameter(names = "-probeHigh", required = false, description = "Highest timing value for probe")
    private Integer probeHi = 1000;

    @Parameter(names = "-probeExport", required = false, description = "Output file for the modified alphabet")
    private String probeExport = null;

    private ProbeTestRunner probeTestRunner = null;
//	private String cmd;

    private Integer lo, hi;

    public TimingProbe() {
    }

    public TimingProbe(ProbeTestRunner probeTestRunner) {
        this.probeTestRunner = probeTestRunner;
    }

    /*
     * findDeterministicTimeValues() finds the lowest values for the parameters
     * supplied in the -timingProbe parameter. This can be timeout, startWait or
     * an alphabet name (such as PSK_CLIENT_HELLO or HELLO_VERIFY_REQUEST). If
     * the parameter is an alphabet name, the extendedWait parameter is found.
     *
     * The search is done by first setting all parameters to the -probeHigh value
     * and then finding the first value leading to deterministic results using
     * a form of binary search.
     */
    public Map<String, Integer> findDeterministicTimesValues() throws IOException, ProbeException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        String[] cmds = probeCmd.split(",");
        setAllTimingParameters(cmds);
        // do a control run, throw exception if non-deterministic
        if (probeTestRunner.isNonDeterministic(true))
            throw new ProbeException("Non-determinism at max timing values");

        for (String cmd : cmds) {
            Integer bestTime;
            if (findLimits(cmd))
                bestTime = probeLo;
            else
                bestTime = binarySearch(cmd);
            map.put(cmd, bestTime);
            setTimingParameter(cmd, bestTime);
        }
        return map;
    }

    private void setAllTimingParameters(String[] cmds) throws IllegalArgumentException {
        for (String cmd : cmds) {
            setTimingParameter(cmd, probeHi);
        }
    }

    private void setTimingParameter(String cmd, Integer time) throws IllegalArgumentException {
        if (cmd.contains("responseWait")) {
            probeTestRunner.getConfig().getSulDelegate().setResponseWait(time);
        }
        else if (cmd.contains("startWait"))
            probeTestRunner.getConfig().getSulDelegate().setStartWait(Long.valueOf(time));
        else {
            for (TlsInput in : probeTestRunner.getAlphabet()) {
                if (in.toString().contains(cmd))
                    in.setExtendedWait(time);
            }
        }
    }

    /*
     * findLimits sets hi to the first deterministic value encountered
     * (found by doubling hi each iteration) and lo to the last non-deterministic
     * returns true iff deterministic on the first try
     */
    private boolean findLimits(String cmd) throws IOException {
        hi = probeLo;
        lo = probeLo;
        boolean keepSearching;
        if (cmd.contains("responseWait") && hi == 0)
            keepSearching = true;
        else {
            setTimingParameter(cmd, hi);
            keepSearching = probeTestRunner.isNonDeterministic(false);
        }

        if (!keepSearching)
            return true;
        if (probeLo > 0)
            hi = probeLo;
        else {
            hi = probeMin;
            setTimingParameter(cmd, hi);
            keepSearching = probeTestRunner.isNonDeterministic(false);
        }
        while (keepSearching && hi < probeHi) {
            lo = hi;
            hi = hi * 2;
            setTimingParameter(cmd, hi);
            keepSearching = probeTestRunner.isNonDeterministic(false);
        }
        return false;
    }

    /*
     * binarySearch refines the search for deterministic value by using a binary search
     * [lo, hi] is the range of the search interval
     */
    private Integer binarySearch(String cmd) throws IOException, IllegalArgumentException {
        while (hi - lo > probeMin) {
            Integer mid = lo + (hi - lo) / 2;
            setTimingParameter(cmd, mid);
            if (probeTestRunner.isNonDeterministic(false))
                lo = mid;
            else
                hi = mid;
        }
        return hi;
    }

    public ProbeTestRunner getProbeTestRunner() {
        return probeTestRunner;
    }

    public void setProbeTestRunner(ProbeTestRunner probeTestRunner) {
        this.probeTestRunner = probeTestRunner;
    }

    public String getProbeCmd() {
        return probeCmd;
    }

    public void setProbeCmd(String probeCmd) {
        this.probeCmd = probeCmd;
    }

    public final boolean isActive() {
        return probeCmd != null;
    }

    public boolean isValid() {
        String[] cmds = probeCmd.split(",");
        for (String cmd : cmds) {
            if (!isValid(cmd))
                return false;
        }
        return true;
    }

    public boolean isValid(String cmd) {
        if (cmd.contains("responseWait") || cmd.contains("startWait"))
            return true;
        for (TlsInput in : probeTestRunner.getAlphabet()) {
            if (in.toString().contains(cmd))
                return true;
        }
        return false;
    }

    public void exportAlphabet() throws FileNotFoundException, IOException, JAXBException {
        if (probeExport != null) {
            FileOutputStream alphabetStream = new FileOutputStream(probeExport);
            AlphabetSerializer.write(alphabetStream, probeTestRunner.getAlphabet());
        }
    }

    public static String present(Map<String, Integer> map) {
        return map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
    }
    }
