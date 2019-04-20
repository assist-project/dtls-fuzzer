package se.uu.it.modeltester.learn;

/**
 * The testing algorithms. Random walk is the simplest, but performs badly on large models:
 * the chance of hitting a erroneous long trace is very small. WMethod and WpMethod are
 * smarter. UserQueries asks the user for which inputs to try as counter-example: have a
 * look at the hypothesis, and try to think of one
 */
public enum EquivalenceAlgorithmName { 
	W_METHOD, MODIFIED_W_METHOD, WP_METHOD, RANDOM_WORDS, RANDOM_WALK, RANDOM_WP_METHOD 
}
