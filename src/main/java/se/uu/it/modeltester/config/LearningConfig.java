package se.uu.it.modeltester.config;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.beust.jcommander.Parameter;

import se.uu.it.modeltester.learn.EquivalenceAlgorithmName;
import se.uu.it.modeltester.learn.LearningAlgorithmName;

public class LearningConfig {
	@Parameter(names = "-learningAlgorithm", description = "Which algorithm shold be used for learning")
	private LearningAlgorithmName learningAlgorithm = LearningAlgorithmName.TTT;

	@Parameter(names = "-equivalenceAlgorithms", description = "Which test algorithms should be used for equivalance testing")
	private List<EquivalenceAlgorithmName> equivalenceAlgorithms = Arrays
			.asList(EquivalenceAlgorithmName.RANDOM_WP_METHOD);

	@Parameter(names = "-depth", description = "Maximal depth ( W/WP Method)")
	private int maxDepth = 1;

	@Parameter(names = "-minLength", description = "Min length (random words, Random WP Method)")
	private int minLength = 5;

	@Parameter(names = "-maxLength", description = "Max length (random words)")
	private int maxLength = 15;

	@Parameter(names = "-randLength", description = "Size of the random part (Random WP Method)")
	private int randLength = 5;

	@Parameter(names = "-queries", description = "Number of queries (all)")
	private int numberOfQueries = 1000;

	@Parameter(names = "-probReset", description = "Probability of stopping execution of a test after each input")
	private int probReset = 0;

	@Parameter(names = "-testFile", description = "A file with tests to be run.")
	private String testFile;

	@Parameter(names = "-seed", description = "Seed used for random value generation.")
	private long seed = new Random().nextLong();

	@Parameter(names = "-logQueries", description = "Log queries to an output false")
	private boolean logQueries = false;

	public boolean doLogQueries() {
		return logQueries;
	}

	public LearningAlgorithmName getLearningAlgorithm() {
		return learningAlgorithm;
	}

	public List<EquivalenceAlgorithmName> getEquivalenceAlgorithms() {
		return equivalenceAlgorithms;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public int getMinLength() {
		return minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public int getNumberOfQueries() {
		return numberOfQueries;
	}

	public int getProbReset() {
		return probReset;
	}

	public int getRandLength() {
		return randLength;
	}

	public String getTestFile() {
		return testFile;
	}

	public long getSeed() {
		return seed;
	}

}
