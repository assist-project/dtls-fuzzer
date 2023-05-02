package se.uu.it.dtlsfuzzer;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.SULOracle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.uu.it.dtlsfuzzer.config.MapperConfig;
import se.uu.it.dtlsfuzzer.config.SulDelegate;
import se.uu.it.dtlsfuzzer.config.TestRunnerConfig;
import se.uu.it.dtlsfuzzer.config.TestRunnerEnabler;
import se.uu.it.dtlsfuzzer.mapper.PhasedMapper;
import se.uu.it.dtlsfuzzer.sut.TlsSULBuilder;
import se.uu.it.dtlsfuzzer.sut.input.AlphabetFactory;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.ModelOutputs;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class TestRunner {
    private static final Logger LOGGER = LogManager.getLogger();
    private MealyMembershipOracle<TlsInput, TlsOutput> sulOracle;
    private TestRunnerConfig config;
    private Alphabet<TlsInput> alphabet;
    private MealyMachine<?, TlsInput, ?, TlsOutput> testSpecification;
    private CleanupTasks cleanupTasks;

    /**
     * Instantiates, executes the tests in the config file and cleans up left-over processes once it is done.
     */
    public static void runTestRunner(TestRunnerEnabler config) throws IOException {
        CleanupTasks cleanupTasks = new CleanupTasks();
        Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
        TestRunner runner = null;
        try {
            runner = new TestRunner(config.getTestRunnerConfig(), alphabet, config.getSulDelegate(), config.getMapperConfig(), cleanupTasks);
            List<TestRunnerResult<TlsInput, TlsOutput>> results = runner.runTests();
            for (TestRunnerResult<TlsInput, TlsOutput> result : results) {
                LOGGER.info(result.toString());
                if (config.getTestRunnerConfig().isShowTransitionSequence()) {
                    LOGGER.info("");
                    LOGGER.info("Displaying Transition Sequence");
                    LOGGER.info("");
                    LOGGER.info(getTransitionSequenceString(result, !config.getSulDelegate().isClient()));
                }
            }

        } finally {
            if (runner != null) {
                runner.terminate();
            }
        }
    }

    public static String getTransitionSequenceString(TestRunnerResult<TlsInput, TlsOutput> result, boolean client) {
        StringBuilder sb = new StringBuilder();
        for (Word<TlsOutput> answer : result.getGeneratedOutputs().keySet()) {
            sb.append(System.lineSeparator());
            for (int i=0; i<result.getInputWord().size(); i++) {

                List<TlsOutput> atomicOutputs = new LinkedList<>(answer.getSymbol(i).getAtomicOutputs(2));
                if (client && i == 0 && ModelOutputs.hasClientHello(atomicOutputs.get(0))) {
                    sb.append("- / ").append(atomicOutputs.get(0)).append(System.lineSeparator());
                    atomicOutputs.remove(0);
                }
                sb.append(result.getInputWord().getSymbol(i)).append(" / ");
                if (answer.getSymbol(i).isTimeout() || atomicOutputs.isEmpty())  {
                    sb.append("-");
                } else {
                    atomicOutputs.forEach(ao -> sb.append(ao).append("; "));
                    sb.deleteCharAt(sb.length()-2);
                }
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public  TestRunner(TestRunnerConfig config, Alphabet<TlsInput> alphabet, SulDelegate sulDelegate, MapperConfig mapperConfig, CleanupTasks cleanupTasks) throws IOException {
        this.config = config;
        this.alphabet = alphabet;
        sulOracle = createTestOracle(sulDelegate, mapperConfig, cleanupTasks);
        if (config.getTestSpecification() != null) {
            testSpecification = ModelFactory.buildTlsModel(alphabet, config.getTestSpecification());
        }
        this.cleanupTasks = cleanupTasks;
    }

    protected Alphabet<TlsInput> getAlphabet() {
        return alphabet;
    }

    public List<TestRunnerResult<TlsInput, TlsOutput>> runTests()
            throws IOException{
        TestParser testParser = new TestParser();
        List<Word<TlsInput>> tests;
        if (new File(config.getTest()).exists()) {
            tests = testParser.readTests(alphabet,
                config.getTest());
        } else {
            LOGGER.info("File {} does not exist, interpreting argument as test", config.getTest());
            String[] testStrings = config.getTest().split("\\s+");
            tests = Arrays.asList(testParser.readTest(alphabet, Arrays.asList(testStrings)));
        }
        List<TestRunnerResult<TlsInput, TlsOutput>> results = new LinkedList<>();
        for (Word<TlsInput> test : tests) {
            TestRunnerResult<TlsInput, TlsOutput> result = runTest(test);
            results.add(result);
        }
        return results;
    }

    private TestRunnerResult<TlsInput, TlsOutput> runTest(Word<TlsInput> test) throws FileNotFoundException {
        TestRunnerResult<TlsInput, TlsOutput> result = runTest(test,
                config.getTimes(), sulOracle);
        if (testSpecification != null) {
            Word<TlsOutput> outputWord = testSpecification.computeOutput(test);
            result.setExpectedOutputWord(outputWord);
        }
        return result;
    }

    public static <I, O> TestRunnerResult<I, O> runTest(Word<I> test,
            int times, MealyMembershipOracle<I, O> sulOracle) {
        LinkedHashMap<Word<O>, Integer> answerMap = new LinkedHashMap<>();
        for (int i = 0; i < times; i++) {
            Word<O> answer = sulOracle.answerQuery(test);
            if (!answerMap.containsKey(answer)) {
                answerMap.put(answer, 1);
            } else {
                answerMap.put(answer, answerMap.get(answer) + 1);
            }
        }
        return new TestRunnerResult<I, O>(test, answerMap);
    }

    private MealyMembershipOracle<TlsInput, TlsOutput> createTestOracle(
            SulDelegate sulDelegate, MapperConfig mapperConfig, CleanupTasks cleanupTasks) {
        TlsSULBuilder builder = new TlsSULBuilder(sulDelegate, mapperConfig, new PhasedMapper(mapperConfig), cleanupTasks);

        SUL<TlsInput, TlsOutput> sut = builder.getWrappedTlsSUL();

        MealyMembershipOracle<TlsInput, TlsOutput> tlsOracle = new SULOracle<TlsInput, TlsOutput>(sut);

        return tlsOracle;
    }

    /**
     * Cleans up any left-over SUL process. Should be called only after all the desired tests have been executed.
     */
    public void terminate() {
        cleanupTasks.execute();
    }

}
