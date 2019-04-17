package de.rub.nds.modelfuzzer.config;

import com.beust.jcommander.Parameter;

import de.rub.nds.modelfuzzer.learn.EquivalenceAlgorithmName;
import de.rub.nds.modelfuzzer.learn.LearningAlgorithmName;


public class LearningConfig {
	@Parameter(names = "-learningAlgorithm", description = "Which algorithm shold be used for learning")
	private LearningAlgorithmName learningAlgorithm = LearningAlgorithmName.TTT;

	@Parameter(names = "-equivalenceAlgorithm", description = "Which algorithm shold be used for equivalance testing")
	private EquivalenceAlgorithmName equivalenceAlgorithm = EquivalenceAlgorithmName.RANDOM_WP_METHOD;

	@Parameter(names = "-depth", description = "Maximal depth ( W/WP Method)")
	private int maxDepth = 4;

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


	public LearningAlgorithmName getLearningAlgorithm() {
		return learningAlgorithm;
	}

	public EquivalenceAlgorithmName getEquivalenceAlgorithm() {
		return equivalenceAlgorithm;
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

}
