package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapperStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerComposerStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbe;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;
import se.uu.it.dtlsfuzzer.config.DtlsSulClientConfig;
import se.uu.it.dtlsfuzzer.config.DtlsSulServerConfig;
import se.uu.it.dtlsfuzzer.mapper.PhasedMapper;
import se.uu.it.dtlsfuzzer.sut.TlsSULBuilder;
import se.uu.it.dtlsfuzzer.sut.input.AlphabetFactory;

public class MultiBuilder implements StateFuzzerConfigBuilder, StateFuzzerBuilder, TestRunnerBuilder, TimingProbeBuilder {

    protected AlphabetBuilder alphabetBuilder = new AlphabetFactory();
    private MapperConfig mapperConfig = new MapperConfig();

 // SulBuilderImpl needs to be implemented
    protected SulBuilder sulBuilder = new TlsSULBuilder(mapperConfig, new PhasedMapper(mapperConfig));
    protected SulWrapper sulWrapper = new SulWrapperStandard();

    MultiBuilder() {

    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        return new TimingProbe(timingProbeEnabler, alphabetBuilder, sulBuilder, sulWrapper);
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        return new TestRunner(testRunnerEnabler, alphabetBuilder, sulBuilder, sulWrapper);
    }

    @Override
    public StateFuzzer build(StateFuzzerEnabler stateFuzzerEnabler) {
        return new StateFuzzerStandard(new StateFuzzerComposerStandard(stateFuzzerEnabler, alphabetBuilder, sulBuilder, sulWrapper));
    }

    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfig(new DtlsSulClientConfig());
    }

    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfig(new DtlsSulServerConfig());
    }

}
