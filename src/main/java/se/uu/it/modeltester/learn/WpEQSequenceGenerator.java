package se.uu.it.modeltester.learn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Sequence generation method factored out from the RandomWpMethodEQOracle.
 */
public class WpEQSequenceGenerator<I, D, S> {

	private UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton;
	private Collection<? extends I> inputs;
	private ArrayList<Word<I>> globalSuffixes;
	private MutableMapping<S, ArrayList<Word<I>>> localSuffixSets;

	public WpEQSequenceGenerator(
			UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs) {
		this.automaton = automaton;
		this.inputs = inputs;
		globalSuffixes = computeGlobalSuffixes(automaton, inputs);
		localSuffixSets = computeLocalSuffixSets(automaton, inputs);
	}

	private ArrayList<Word<I>> computeGlobalSuffixes(
			UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs) {
		ArrayList<Word<I>> globalSuffixes = new ArrayList<>();
		Automata.characterizingSet(automaton, inputs, globalSuffixes);
		return globalSuffixes;
	}

	private MutableMapping<S, ArrayList<Word<I>>> computeLocalSuffixSets(
			UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs) {
		MutableMapping<S, ArrayList<Word<I>>> localSuffixSets = automaton
				.createStaticStateMapping();
		for (S state : automaton.getStates()) {
			ArrayList<Word<I>> suffixSet = new ArrayList<>();
			Automata.stateCharacterizingSet(automaton, inputs, state, suffixSet);
			localSuffixSets.put(state, suffixSet);
		}
		return localSuffixSets;
	}

	Word<I> getRandomMiddleSequence(int minimalSize, int rndLength, Random rand) {
		ArrayList<I> arrayAlphabet = new ArrayList<>(inputs);
		WordBuilder<I> wb = new WordBuilder<>();
		// construct random middle part (of some expected length)
		int size = minimalSize;
		while ((size > 0) || (rand.nextDouble() > 1 / (rndLength + 1.0))) {
			wb.append(arrayAlphabet.get(rand.nextInt(arrayAlphabet.size())));
			if (size > 0)
				size--;
		}
		return wb.toWord();
	}

	Word<I> getRandomDistinguishingSequence(Random rand) {
		Word<I> accSeq = getRandomDistinguishingSequence(automaton, inputs,
				rand);
		return accSeq;
	}

	private Word<I> getRandomDistinguishingSequence(
			UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs, Random rand) {
		WordBuilder<I> wb = new WordBuilder<>();

		// pick a random suffix for this state
		// 50% chance for state testing, 50% chance for transition testing
		if (rand.nextBoolean()) {
			// global
			if (!globalSuffixes.isEmpty()) {
				wb.append(globalSuffixes.get(rand.nextInt(globalSuffixes.size())));
			}
		} else {
			// local
			S state2 = automaton.getState(wb);
			ArrayList<Word<I>> localSuffixes = localSuffixSets.get(state2);
			if (!localSuffixes.isEmpty()) {
				wb.append(localSuffixes.get(rand.nextInt(localSuffixes.size())));
			}
		}
		return wb.toWord();
	}

	Word<I> getRandomAccessSequence(S toState, Random rand) {
		Word<I> accSeq = getRandomAccessSequence(automaton, inputs, toState,
				rand);
		return accSeq;
	}

	private Word<I> getRandomAccessSequence(
			UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs, S toState, Random rand) {
		Queue<S> bfsQueue = new ArrayDeque<S>();
		MutableMapping<S, Word<I>> reach = automaton.createStaticStateMapping();
		S init = automaton.getInitialState();
		reach.put(init, Word.<I> epsilon());
		bfsQueue.add(init);

		S curr;

		while ((curr = bfsQueue.poll()) != null && curr != toState) {
			Word<I> as = reach.get(curr);
			List<I> shuffledInputs = new ArrayList<>(inputs);
			Collections.shuffle(shuffledInputs, rand);

			for (I in : inputs) {
				S succ = automaton.getSuccessor(curr, in);
				if (succ == null)
					continue;

				if (reach.get(succ) == null) {
					Word<I> succAs = as.append(in);
					reach.put(succ, succAs);
				}
				bfsQueue.add(succ);
			}
		}

		return reach.get(curr);
	}
}
