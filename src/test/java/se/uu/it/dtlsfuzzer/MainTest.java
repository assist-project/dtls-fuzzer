package se.uu.it.dtlsfuzzer;

import static se.uu.it.dtlsfuzzer.config.ToolName.STATE_FUZZER_CLIENT;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import se.uu.it.dtlsfuzzer.config.StateFuzzerClientConfig;
import se.uu.it.dtlsfuzzer.learner.EquivalenceAlgorithmName;

public class MainTest {
    /*
     * Tests if parsing works for valid arguments which do not set dynamic parameters.
     */
    @Test
    public void testParseValidArguments() {
        String sutCommand = "some command to run SUT";
        ParsingResult result = Main.parseArguments(new String [] {
                STATE_FUZZER_CLIENT.getName(),
                "-port",
                "2000",
                "-cmd",
                sutCommand,
                "-equivalenceAlgorithms",
                EquivalenceAlgorithmName.RANDOM_WALK.name() + "," + EquivalenceAlgorithmName.RANDOM_WORDS.name(),
        });

        Assert.assertTrue(result.getParsedConfig() instanceof StateFuzzerClientConfig);
        Assert.assertEquals(sutCommand, result.getParsedConfig().getSulDelegate().getCommand());
        Assert.assertEquals(Arrays.asList(EquivalenceAlgorithmName.RANDOM_WALK, EquivalenceAlgorithmName.RANDOM_WORDS),
                result.getParsedConfig().getLearningConfig().getEquivalenceAlgorithms());
    }

    /*
     * Tests if parsing works for valid arguments which set dynamic parameters used as placeholder variables.
     */
    @Test
    public void testParseValidArgumentsWithDynamicParameters() {
        String sutCommand = "some command with ${variable} to run SUT";
        ParsingResult result = Main.parseArguments(new String [] {
                STATE_FUZZER_CLIENT.getName(),
                "-port",
                "2000",
                "-Dvariable=value",
                "-cmd",
                sutCommand,
                "-equivalenceAlgorithms",
                EquivalenceAlgorithmName.RANDOM_WALK.name()  + "," + EquivalenceAlgorithmName.RANDOM_WORDS.name(),
        });

        Assert.assertTrue(result.getParsedConfig() instanceof StateFuzzerClientConfig);
        Assert.assertEquals("some command with value to run SUT", result.getParsedConfig().getSulDelegate().getCommand());
        Assert.assertEquals(Arrays.asList(EquivalenceAlgorithmName.RANDOM_WALK, EquivalenceAlgorithmName.RANDOM_WORDS),
                result.getParsedConfig().getLearningConfig().getEquivalenceAlgorithms());
    }

    /*
     * Tests that parsing returns a null object if the arguments supplied are invalid.
     */
    @Test
    public void testParseInvalidArguments() {
        ParsingResult result = Main.parseArguments(new String [] {
                STATE_FUZZER_CLIENT.getName(),
                "-invalidArgument",});
        Assert.assertNull(result);
    }
}
