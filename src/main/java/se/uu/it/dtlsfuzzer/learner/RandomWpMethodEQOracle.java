package se.uu.it.dtlsfuzzer.learner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Adapted from an EQ oracle implementation in LearnLib's development branch not
 * available in the version we use.
 * 
 * <pre>
 * See: <a href="https://github.com/mtf90/learnlib/blob/develop/eqtests/
 * 	basic-eqtests/src/main/java/de/learnlib/eqtests/basic/RandomWpMethodEQOracle.java">RandomWpMethodEQOracle</a>
 * </pre>
 * <p/>
 * Implements an equivalence test by applying the Wp-method test on the given
 * hypothesis automaton, as described in
 * "Test Selection Based on Finite State Models" by S. Fujiwara et al. Instead
 * of enumerating the test suite in order, this is a sampling implementation: 1.
 * sample uniformly from the states for a prefix 2. sample geometrically a
 * random word 3. sample a word from the set of suffixes / state identifiers
 * (either local or global) There are two parameters: minimalSize determines the
 * minimal size of the random word, this is useful when one first performs a
 * W(p)-method with some depth and continue with this randomized tester from
 * that depth onward. The second parameter rndLength determines the expected
 * length of the random word. (The expected length in effect is minimalSize +
 * rndLength.) In the unbounded case it will not terminate for a correct
 * hypothesis.
 * 
 * Our adaptation is randomizing access sequence generation.
 *
 * @param <A>
 *            automaton type
 * @param <I>
 *            input symbol type
 * @param <D>
 *            output domain type
 * @author Joshua Moerman
 */
public class RandomWpMethodEQOracle<I,O>
		implements
		EquivalenceOracle.MealyEquivalenceOracle<I, O> {
	private final MealyMembershipOracle<I, O>  sulOracle;
	private final int minimalSize;
	private final int rndLength;
	private final int bound;
	private long seed;

	/**
	 * Constructor for an unbounded testing oracle
	 *
	 * @param sulOracle
	 *            oracle which answers tests.
	 * @param minimalSize
	 *            minimal size of the random word
	 * @param rndLength
	 *            expected length (in addition to minimalSize) of random word
	 */
	public RandomWpMethodEQOracle(MealyMembershipOracle<I, O> sulOracle,
			int minimalSize, int rndLength, long seed) {
		this.sulOracle = sulOracle;
		this.minimalSize = minimalSize;
		this.rndLength = rndLength;
		this.seed = seed;
		this.bound = 0;
	}

	/**
	 * Constructor for a bounded testing oracle
	 *
	 * @param sulOracle
	 *            oracle which answers tests.
	 * @param minimalSize
	 *            minimal size of the random word
	 * @param rndLength
	 *            expected length (in addition to minimalSize) of random word
	 * @param bound
	 *            specifies the bound (set to 0 for unbounded).
	 */
	public RandomWpMethodEQOracle(MealyMembershipOracle<I, O> sulOracle,
			int minimalSize, int rndLength, int bound, long seed) {
		this.sulOracle = sulOracle;
		this.minimalSize = minimalSize;
		this.rndLength = rndLength;
		this.bound = bound;
		this.seed = seed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object,
	 * java.util.Collection)
	 */
	@Override
	public @Nullable DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
			Collection<? extends I> inputs) {
		return doFindCounterExample(hypothesis, inputs);
	}
	
	public <S> @Nullable DefaultQuery<I, Word<O>> doFindCounterExample(MealyMachine<S, I, ?, O> hypothesis,
			Collection<? extends I> inputs) {
		WpEQSequenceGenerator<I, Word<O>, S> generator = new WpEQSequenceGenerator<>(
				hypothesis, inputs);

		Random rand = new Random(seed);
		List<S> states = new ArrayList<>(hypothesis.getStates());

		int currentBound = bound;
		while (bound == 0 || currentBound-- > 0) {
			WordBuilder<I> wb = new WordBuilder<>(minimalSize + rndLength + 1);

			// pick a random state
			wb.append(generator.getRandomAccessSequence(
					states.get(rand.nextInt(states.size())), rand));

			// construct random middle part (of some expected length)
			wb.append(generator.getRandomMiddleSequence(minimalSize, rndLength,
					rand));

			// construct a random characterizing/identifying sequence
			wb.append(generator.getRandomCharacterizingSequence(wb, rand));

			Word<I> queryWord = wb.toWord();
			Word<O> hypOutput = hypothesis.computeOutput(queryWord);
			DefaultQuery<I, Word<O>> query = new DefaultQuery<>(queryWord);
			sulOracle.processQueries(Collections.singleton(query));
			if (!Objects.equals(hypOutput, query.getOutput()))
				return query;
		}

		// no counter example found within the bound
		return null;
	}
}