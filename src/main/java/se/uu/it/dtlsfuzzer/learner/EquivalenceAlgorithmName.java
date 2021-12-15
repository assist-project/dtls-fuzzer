package se.uu.it.dtlsfuzzer.learner;

/**
 * The testing algorithms. Random walk is the simplest, but performs badly on
 * large models: the chance of hitting a erroneous long trace is very small.
 * WMethod and WpMethod are smarter.
 */
public enum EquivalenceAlgorithmName {
	W_METHOD, MODIFIED_W_METHOD, WP_METHOD, RANDOM_WORDS, RANDOM_WALK, RANDOM_WP_METHOD, SAMPLED_TESTS, WP_SAMPLED_TESTS;
}
