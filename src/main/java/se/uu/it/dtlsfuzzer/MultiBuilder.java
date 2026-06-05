package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerComposerStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTester;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbe;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;
import se.uu.it.dtlsfuzzer.components.sul.core.TlsSulBuilder;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulClientConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulServerConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsAlphabetPojoXml;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class MultiBuilder implements StateFuzzerConfigBuilder, DiffTesterConfigBuilder,
        StateFuzzerBuilder<MealyMachineWrapper<TlsInput, TlsOutput>>, DiffTesterBuilder, TestRunnerBuilder, TimingProbeBuilder {

    private AlphabetBuilder<TlsInput> alphabetBuilder = new AlphabetBuilderStandard<>(
            new AlphabetSerializerXml<>(TlsInput.class, TlsAlphabetPojoXml.class));

    private SULBuilder<TlsInput, TlsOutput, TlsExecutionContext> sulBuilder = new TlsSulBuilder();

    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfigStandard(
                new LearnerConfigStandard(),
                new TlsSulClientConfig(),
                new TestRunnerConfigStandard(),
                new TimingProbeConfigStandard());
    }

    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfigStandard(
                new LearnerConfigStandard(),
                new TlsSulServerConfig(),
                new TestRunnerConfigStandard(),
                new TimingProbeConfigStandard()
        );
    }

    @Override
    public DiffTesterConfig buildConfig() {
        return new DiffTesterConfigStandard();
    }

    @Override
    public StateFuzzer<MealyMachineWrapper<TlsInput, TlsOutput>> build(StateFuzzerEnabler stateFuzzerEnabler) {
        return new StateFuzzerStandard<>(
            new StateFuzzerComposerStandard<>(stateFuzzerEnabler, alphabetBuilder, sulBuilder).initialize()
        );
    }

    @Override
    public DiffTester build(DiffTesterEnabler diffTesterEnabler) {
        return new DiffTesterStandard<>(diffTesterEnabler, alphabetBuilder);
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        return new TestRunnerStandard<>(testRunnerEnabler, alphabetBuilder, sulBuilder).initialize();
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        return new TimingProbeStandard<>(timingProbeEnabler, alphabetBuilder, sulBuilder).initialize();
    }
}
