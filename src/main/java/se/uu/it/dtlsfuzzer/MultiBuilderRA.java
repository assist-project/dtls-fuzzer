package se.uu.it.dtlsfuzzer;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.xml.AlphabetSerializerXml;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapperStandard;
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
import java.util.Map;
import se.uu.it.dtlsfuzzer.components.sul.core.TlsSulBuilderRA;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulClientConfig;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulServerConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsAlphabetPojoXml;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;

public class MultiBuilderRA implements StateFuzzerConfigBuilder,
                StateFuzzerBuilder<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>>, TestRunnerBuilder,
                TimingProbeBuilder {

        private AlphabetBuilder<ParameterizedSymbol> alphabetBuilder = new AlphabetBuilderStandard<ParameterizedSymbol>(
                        new AlphabetSerializerXml<ParameterizedSymbol, TlsAlphabetPojoXml>(TlsInput.class,
                                        TlsAlphabetPojoXml.class));

        // SulBuilderImpl needs to be implemented
        private SulBuilder<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> sulBuilder = new TlsSulBuilderRA();
        private SulWrapper<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> sulWrapper = new SulWrapperStandard<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA>();

        MultiBuilderRA() {
        }

        @Override
        public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
                return null;
                // return new TimingProbeStandard<TlsInput, TlsOutput, TlsProtocolMessage,
                // TlsExecutionContext>(
                // timingProbeEnabler,
                // alphabetBuilder, sulBuilder, sulWrapper);
        }

        @Override
        public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
                return null;
                // return new TestRunnerStandard<TlsInput, TlsOutput, TlsProtocolMessage,
                // TlsExecutionContext>(
                // testRunnerEnabler,
                // alphabetBuilder, sulBuilder, sulWrapper);
        }

        @Override
        public StateFuzzerRA<ParameterizedSymbol, TlsExecutionContextRA> build(
                        StateFuzzerEnabler stateFuzzerEnabler) {
                Map<DataType, Theory> teachers = null;
                StateFuzzerComposerRA<ParameterizedSymbol, TlsExecutionContextRA> composer = new StateFuzzerComposerRA<ParameterizedSymbol, TlsExecutionContextRA>(
                                stateFuzzerEnabler, alphabetBuilder, sulBuilder, sulWrapper, teachers);
                composer.initialize();
                return new StateFuzzerRA<>(composer);
        }

        @Override
        public StateFuzzerClientConfig buildClientConfig() {
                return new StateFuzzerClientConfigStandard(new LearnerConfigStandard(), new TlsSulClientConfig(),
                                new TestRunnerConfigStandard(), new TimingProbeConfigStandard());
        }

        @Override
        public StateFuzzerServerConfig buildServerConfig() {
                return new StateFuzzerServerConfigStandard(new LearnerConfigStandard(), new TlsSulServerConfig(),
                                new TestRunnerConfigStandard(), new TimingProbeConfigStandard());
        }

}
