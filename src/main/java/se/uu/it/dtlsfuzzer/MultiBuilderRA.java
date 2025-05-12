package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigRA;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapperStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerComposerRA;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerRA;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbe;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import java.util.LinkedHashMap;
import java.util.Map;
import se.uu.it.dtlsfuzzer.components.sul.core.TlsSulBuilderRA;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulClientConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulServerConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.TlsInputTransformer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsAlphabetPojoXml;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class MultiBuilderRA
    implements
        StateFuzzerConfigBuilder,
        StateFuzzerBuilder<
            RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>
        >,
        TestRunnerBuilder,
        TimingProbeBuilder {

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

    protected SulBuilder<
        PSymbolInstance,
        PSymbolInstance,
        TlsExecutionContextRA
    > sulBuilder = new TlsSulBuilderRA(inputTransformer);

    protected SulWrapper<
        PSymbolInstance,
        PSymbolInstance,
        TlsExecutionContextRA
    > sulWrapper = new SulWrapperStandard<>();

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
                sulWrapper,
                teachers
            ).initialize()
        );
    }

    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfigStandard(
            new LearnerConfigRA(),
            new TlsSulClientConfig(),
            new TestRunnerConfigStandard(),
            new TimingProbeConfigStandard()
        );
    }

    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfigStandard(
            new LearnerConfigRA(),
            new TlsSulServerConfig(),
            new TestRunnerConfigStandard(),
            new TimingProbeConfigStandard()
        );
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        // FIXME: functionality does not yet exist for RA-learning
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        // FIXME: functionality does not yet exist for RA-learning
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }
}
