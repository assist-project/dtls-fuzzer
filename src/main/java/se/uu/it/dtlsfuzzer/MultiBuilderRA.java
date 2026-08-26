package se.uu.it.dtlsfuzzer;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigRA;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerComposerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTester;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbe;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;
import java.util.LinkedHashMap;
import java.util.Map;
import se.uu.it.dtlsfuzzer.components.sul.core.TlsSULBuilderRA;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSULClientConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSULServerConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsInputTransformer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsAlphabetPojoXml;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class MultiBuilderRA
    implements
        StateFuzzerConfigBuilder, DiffTesterConfigBuilder, StateFuzzerBuilder<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>>,
            DiffTesterBuilder, TestRunnerBuilder, TimingProbeBuilder {

    protected AlphabetBuilderStandard<TlsInput> standardBuilder =
        new AlphabetBuilderStandard<>(
            new AlphabetSerializerXml<>(
                TlsInput.class,
                TlsAlphabetPojoXml.class
            )
        );

    protected TlsInputTransformer inputTransformer = new TlsInputTransformer(
        standardBuilder
    );

    protected SULBuilder<
        PSymbolInstance,
        PSymbolInstance,
        TlsExecutionContextRA
    > sulBuilder = new TlsSULBuilderRA(inputTransformer);

    @Override
    public StateFuzzer<
        RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>
    > build(StateFuzzerEnabler stateFuzzerEnabler) {
        @SuppressWarnings("rawtypes") // TODO: PSF uses Theory without type parameters, check if Theory<?> might be usable or if it requires the map to be homogenus
        final Map<DataType, Theory> teachers = new LinkedHashMap<>();

        return new StateFuzzerRA<>(
            new StateFuzzerComposerRA<
                ParameterizedSymbol,
                TlsExecutionContextRA
            >(
                stateFuzzerEnabler,
                inputTransformer,
                sulBuilder,
                teachers
            ).initialize()
        );
    }

    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfigStandard(
            new LearnerConfigRA(),
            new TlsSULClientConfig(),
            new TestRunnerConfigStandard(),
            new TimingProbeConfigStandard()
        );
    }

    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfigStandard(
            new LearnerConfigRA(),
            new TlsSULServerConfig(),
            new TestRunnerConfigStandard(),
            new TimingProbeConfigStandard()
        );
    }

    @Override
    public DiffTester build(DiffTesterEnabler diffTesterEnabler) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Differential testing support not implemented");
    }

    @Override
    public DiffTesterConfig buildConfig() {
        return new DiffTesterConfigStandard();
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        // FIXME: functionality does not yet exist for RA-learning
        throw new UnsupportedOperationException("TimingProbe support not implemented");
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        return new TestRunnerRA<TlsInput, TlsExecutionContextRA>(testRunnerEnabler, standardBuilder, inputTransformer, sulBuilder);
    }

}
