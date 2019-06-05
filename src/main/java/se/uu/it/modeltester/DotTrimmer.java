package se.uu.it.modeltester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.w3c.dom.css.Counter;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.BasicStringMealyProcessor;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.ListAlphabet;

public class DotTrimmer {
	private static String OTHER="Other";
	private int minCompatible;

	public DotTrimmer(int minCompatible) {
		this.minCompatible = minCompatible;
	}
	
	public FastMealy<String, String> generateSimplifiedMachine(FastMealy<String, String> mealy) {
		Alphabet<String> alphabet = mealy.getInputAlphabet();
		List<String> list = new ArrayList<>(alphabet);
		list.add(OTHER);
		FastMealy<String, String> trimmed = new FastMealy<String, String>(new ListAlphabet<>(list));
		Map<FastMealyState<String>, FastMealyState<String>> stateMap = new HashMap<>();
		for (FastMealyState<String> state : mealy.getStates()) {
			FastMealyState<String> newState = trimmed.addState(null);
			stateMap.put(state, newState);
		}
		constructSimplifiedMachine(mealy, trimmed, stateMap);
		return trimmed;
	}

	private void constructSimplifiedMachine(FastMealy<String, String> mealy, FastMealy<String, String> trimmed,
			Map<FastMealyState<String>, FastMealyState<String>> stateMap) {
		int inputSize = mealy.getInputAlphabet().size();
		for (FastMealyState<String> state : mealy.getStates()) {
			FastMealyState<String> otherState = stateMap.get(state);
			List<MealyTransition<FastMealyState<String>, String>> transitions = transitions(state, inputSize).collect(Collectors.toList());
			Optional<List<MealyTransition<FastMealyState<String>, String>>> maxCompatibleGroup = transitions(state, inputSize)
			.map(t -> getCompatible(state, t, inputSize).collect(Collectors.toList()))
			.max((cl1,cl2) -> cl1.size() > cl2.size()?-1:cl1.size() < cl2.size()?1:0);
			Set<MealyTransition<FastMealyState<String>, String>> excludeTrans = new HashSet<>(); 
			if (maxCompatibleGroup.get().size() > minCompatible) {
				MealyTransition<FastMealyState<String>, String> trans = maxCompatibleGroup.get().get(0);
				MealyTransition<FastMealyState<String>, String> otherTrans = new MealyTransition<FastMealyState<String>, String>(stateMap.get(trans.getSuccessor()), OTHER);
				otherState.setTransition(inputSize, otherTrans);
				excludeTrans.addAll(maxCompatibleGroup.get());
			}
			for (int i=0; i<inputSize; i++) {
				MealyTransition<FastMealyState<String>, String> trans = state.getTransition(i);
				if (!excludeTrans.contains(state.getTransition(i))) {
					MealyTransition<FastMealyState<String>, String> otherTrans = new MealyTransition<FastMealyState<String>, String>(stateMap.get(trans.getSuccessor()), trans.getOutput());
					otherState.setTransition(i, otherTrans);
				}
 			}
		}
	}
	
	
	private Stream<MealyTransition<FastMealyState<String>,String>> getCompatible(FastMealyState<String> state, MealyTransition<FastMealyState<String>, String> tr1, int inputSize) {
		return transitions(state, inputSize).filter(tr -> isCompatible(tr, tr1));
	}
	
	private Stream<MealyTransition<FastMealyState<String>, String>> transitions(FastMealyState<String> state, int inputSize) {
		return IntStream.range(0, inputSize)
				.mapToObj(i -> state.getTransition(i));
		
	}
	
	private boolean isCompatible(MealyTransition<FastMealyState<String>, String> tr1, MealyTransition<FastMealyState<String>, String> tr2) {
		return tr1.getOutput().equals(tr2.getOutput()) && tr1.getSuccessor().equals(tr2.getSuccessor());
	}
	
	
	public static void main(String args[]) throws ParseException, IOException {
		MealyDotParser<String,String> parser = new  MealyDotParser<String,String>(new BasicStringMealyProcessor());
		FastMealy<String, String> aut = parser.parseAutomaton("openssl.dot").get(0);
		DotTrimmer trimmer = new DotTrimmer(3);
		FastMealy<String, String> simAut = trimmer.generateSimplifiedMachine(aut);
		File simFile = new File("sim.dot");
		GraphDOT.write(simAut, simAut.getInputAlphabet(), new FileWriter(simFile));
	}
}
