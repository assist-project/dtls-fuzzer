package se.uu.it.dtlsfuzzer.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.equivalence.DeterministicEquivalenceTest;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.util.automata.fsa.MutableDFAs;
import net.automatalib.words.Word;
import net.automatalib.words.impl.ListAlphabet;

public class DFAUtils extends AutomatonUtils {

	/**
	 * Converts a deterministic Mealy machine to an equivalent DFA. Inputs/outputs are mapped to corresponding labels given the provided input and output mappings.
	 * An output can be mapped to zero, one or several labels (which will be chained one after the other in the model).
	 * The end result is an input-complete DFA which is not minimized to preserve resemblance with the original model.
	 * Minimization can be achieved via minimize methods in {@link Automata}.
	 */
	public static <MI, MS, MO, DI, DS, DA extends MutableDFA<DS, DI>> DA convertMealyToDFA(MealyMachine<MS, MI, ?, MO> mealy, Collection<MI> inputs,
			Collection<DI> labels,
			Mapping<MI,DI> inputMapping,
			Mapping<Pair<MS,MO>,List<DI>> outputMapping,
			Map<MS,DS> stateMapping,
			DA dfa) {
		MS mealyState = mealy.getInitialState();
		Map<MS, DS> inputStateMapping = new HashMap<>();
		DS dfaState = dfa.addInitialState(true);
		inputStateMapping.put(mealyState, dfaState);
		inputStateMapping.put(mealyState, dfaState);
		Set<MS> visited = new HashSet<>();
		convertMealyToDFA(mealyState, dfaState, mealy, inputs, inputMapping, outputMapping, inputStateMapping, visited, dfa);
		MutableDFAs.complete(dfa, labels, false, false);
		stateMapping.putAll(inputStateMapping);
		return dfa;
	}

	private static <MI, MS, MO, DI, DS, DA extends MutableDFA<DS, DI>>  void convertMealyToDFA(MS mealyState, DS dfaState, MealyMachine<MS, MI, ?, MO> mealy,
			Collection<MI> inputs, Mapping<MI, DI> inputMapping, Mapping<Pair<MS,MO>, List<DI>> outputMapping, Map<MS, DS> inputStateMapping,
			Set<MS> visited, DA dfa) {
		inputStateMapping.put(mealyState, dfaState);
		DS inputState = dfaState;
		visited.add(mealyState);
		DS nextInputState;
		for (MI input : inputs) {
			DI inputLabel = inputMapping.get(input);
			MO output = mealy.getOutput(mealyState, input);
			MS nextMealyState = mealy.getSuccessor(mealyState, input);

			nextInputState = inputStateMapping.get(nextMealyState);
			if (nextInputState == null) {
				nextInputState = dfa.addState(true);
				inputStateMapping.put(nextMealyState, nextInputState);
			}

			Collection<DI> outputLabels = outputMapping.get(Pair.of(mealyState, output));
			List<DI> labels = new ArrayList<>(outputLabels.size()+1);
			labels.add(inputLabel);
			labels.addAll(outputLabels);

			DS lastState = inputState, nextState;
			for (int i=0; i<labels.size()-1; i++) {
				DI ioLabel = labels.get(i);
				nextState = dfa.addState(true);
				dfa.addTransition(lastState, ioLabel, nextState);
				lastState = nextState;
			}

			dfa.addTransition(lastState, labels.get(labels.size()-1), nextInputState);

			if (!visited.contains(nextMealyState)) {
				convertMealyToDFA(nextMealyState, nextInputState, mealy, inputs, inputMapping, outputMapping, inputStateMapping, visited, dfa);
			}
		}
	}

	public static <I> DFA<?,I> buildRejecting(Collection<I> alphabet) {
		FastDFA<I> rejectingModel = new FastDFA<I>(new ListAlphabet<I>(new ArrayList<>(alphabet)));
		FastDFAState rej = rejectingModel.addInitialState(false);
		for (I label : alphabet) {
			rejectingModel.addTransition(rej, label, rej);
		}
		return rejectingModel;
	}


	public static <S,I> boolean hasAcceptingPaths(S state, DFA<S, I> automaton, Collection<I> inputs) {
		Set<S> reachableStates = new HashSet<>();
		reachableStates(automaton, inputs, state, reachableStates);
		return reachableStates.stream().anyMatch(s -> automaton.isAccepting(s));
	}

	public static <S,I> Word<I> findShortestAcceptingWord( DFA<S, I> automaton, Collection<I> inputs ) {
		DeterministicEquivalenceTest<I> test = new DeterministicEquivalenceTest<I>(DFAs.complete(automaton, new ListAlphabet<I>(new ArrayList<>(inputs))));
		Word<I> accepting = test.findSeparatingWord(buildRejecting(inputs), inputs);
		return accepting;

//		ModelExplorer<S, I> explorer = new ModelExplorer<S, I>(automaton, inputs);
//		List<S> acceptingStates = automaton.getStates().stream()
//				.filter(s -> automaton.isAccepting(s))
//				.collect(Collectors.toList());
//		if (!acceptingStates.isEmpty()) {
//			SearchConfig config = new SearchConfig();
//			config.setStateVisitBound(1);
//			Iterable<Word<I>> words = explorer.wordsToTargetStates(acceptingStates, config);
//			Iterator<Word<I>> iter = words.iterator();
//			if (iter.hasNext()) {
//				return iter.next();
//			}
//		}
//		return null;
	}

	public static <S,I> Word<I> findShortestNonAcceptingPrefix( DFA<S, I> automaton, Word<I> word ) {
		int prefixLen=0;
		S crtState = automaton.getInitialState();
		for (I input : word) {
			if (crtState == null || !automaton.isAccepting(crtState)) {
				return word.prefix(prefixLen);
			}
			prefixLen ++;
			crtState = automaton.getSuccessor(crtState, input);
		}

		if (crtState == null || !automaton.isAccepting(crtState)) {
			return word.prefix(prefixLen);
		}

		return null;
	}
}
