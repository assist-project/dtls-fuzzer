package se.uu.it.dtlsfuzzer;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerComposerStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTester;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintExtraction;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.IdentifierBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.IdentifierStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.SulIdentifier;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.config.IdentifierConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.config.IdentifierEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbe;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;
import se.uu.it.dtlsfuzzer.components.sul.core.TlsSulBuilder;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulClientConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulServerConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsAlphabetPojoXml;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutput;

public class MultiBuilder implements StateFuzzerConfigBuilder, DiffTesterConfigBuilder, FingerprintConfigBuilder,
        StateFuzzerBuilder<MealyMachineWrapper<TlsInput, TlsOutput>>, DiffTesterBuilder, FingerprintBuilder, TestRunnerBuilder, TimingProbeBuilder, IdentifierBuilder<MealyMachineWrapper<TlsInput, TlsOutput>> {

    private AlphabetBuilder<TlsInput> alphabetBuilder = new AlphabetBuilderStandard<>(
            new AlphabetSerializerXml<>(TlsInput.class, TlsAlphabetPojoXml.class));

    private SULBuilder<TlsInput, TlsOutput, TlsExecutionContext> sulBuilder = new TlsSulBuilder();

    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfigStandard(
                new LearnerConfigStandard(),
                new TlsSulClientConfig(),
                new TestRunnerConfigStandard(),
                new TimingProbeConfigStandard(),
                new IdentifierConfigStandard());
    }

    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfigStandard(
                new LearnerConfigStandard(),
                new TlsSulServerConfig(),
                new TestRunnerConfigStandard(),
                new TimingProbeConfigStandard(),
                new IdentifierConfigStandard()
        );
    }

    @Override
    public DiffTesterConfig buildConfig() {
        return new DiffTesterConfigStandard();
    }

    @Override
    public FingerprintConfig buildConfigFing() {
        return new FingerprintConfigStandard();
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
    public FingerprintExtraction build(FingerprintEnabler fingerprintEnabler) {
        return new FingerprintStandard<>(fingerprintEnabler, alphabetBuilder);
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        return new TestRunnerStandard<>(testRunnerEnabler, alphabetBuilder, sulBuilder).initialize();
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        return new TimingProbeStandard<>(timingProbeEnabler, alphabetBuilder, sulBuilder).initialize();
    }

    @Override
    public SulIdentifier<MealyMachineWrapper<TlsInput, TlsOutput>> build(IdentifierEnabler identifierEnabler) {
        return new IdentifierStandard<>(identifierEnabler, alphabetBuilder, sulBuilder).initialize();
    }
}
