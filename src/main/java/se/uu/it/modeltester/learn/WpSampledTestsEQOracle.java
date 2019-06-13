package se.uu.it.modeltester.learn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Word;

/**
 * This EQ Oracle operates similarly to Wp-Random with the difference that the
 * middle sequence is derived from a set of logs. Concretely, the middle
 * sequence is obtained by selecting a suffix of arbitrary length from an
 * arbitrarily chosen log.
 */
public class WpSampledTestsEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
		implements
			EquivalenceOracle<A, I, D> {
	private List<Word<I>> tests;
	private MembershipOracle<I, D> sulOracle;
	private Random rand;
	private int bound;
	private int minimalSize;
	private int rndLength;

	public WpSampledTestsEQOracle(List<Word<I>> tests,
			MembershipOracle<I, D> sulOracle, int minimalSize, int rndLength,
			long seed, int bound) {
		this.tests = tests;
		this.sulOracle = sulOracle;
		this.rand = new Random(seed);
		this.bound = bound;
		this.minimalSize = minimalSize;
		this.rndLength = rndLength;
	}

	@Override
	public DefaultQuery<I, D> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		UniversalDeterministicAutomaton<?, I, ?, ?, ?> aut = hypothesis;
		Output<I, D> out = hypothesis;
		return doFindCounterExample(aut, out, inputs);
	}

	/*
	 * Delegate target, used to bind the state-parameter of the automaton
	 */
	private <S> DefaultQuery<I, D> doFindCounterExample(
			UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
			Output<I, D> output, Collection<? extends I> inputs) {
		WpEQSequenceGenerator<I, D, S> generator = new WpEQSequenceGenerator<>(
				hypothesis, inputs);
		List<S> states = new ArrayList<>(hypothesis.getStates());
		
		for (int i = 0; i < bound; i++) {
			S randState = states.get(rand.nextInt(states.size())); 

			Word<I> randAccSeq = generator.getRandomAccessSequence(randState,
					rand);

			Word<I> middlePart = Word.epsilon();

			if (rand.nextBoolean() && !tests.isEmpty()) {
				Word<I> randTest = tests.get(rand.nextInt(tests.size()));
				Word<I> randSuffix = randTest.suffix(rand.nextInt(randTest
						.length()));
				middlePart = randSuffix;
			} else {
				middlePart = generator.getRandomMiddleSequence(minimalSize,
						rndLength, rand);
			}

			Word<I> distSequence = generator
					.getRandomCharacterizingSequence(randAccSeq.concat(middlePart), rand);

			Word<I> test = randAccSeq.concat(middlePart, distSequence);

			DefaultQuery<I, D> query = new DefaultQuery<>(test);
			D hypOutput = output.computeOutput(test);
			sulOracle.processQueries(Collections.singleton(query));
			if (!Objects.equals(hypOutput, query.getOutput()))
				return query;
		}
		return null;
	}
}
