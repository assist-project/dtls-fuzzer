package se.uu.it.modeltester.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.words.Alphabet;
import se.uu.it.modeltester.DiagnosisTask;
import se.uu.it.modeltester.config.DiagnosisConfig;
import se.uu.it.modeltester.mutate.MutatingTlsInput;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

public class Diagnoser {
	
	/*
	 * For each input stores 
	 */
	private Map<TlsInput,List<FastMealyState<TlsOutput>>> supportMap;
	private double unsupportedStateRatio = 0.3;
	private DiagnosisConfig config;
	
	public Diagnoser(DiagnosisConfig config) {
		this.config = config;
	}
	
	public Diagnosis diagnoze(DiagnosisTask task, TestReport report) {
		List<FragmentationBug> fragBugs = report.getBugs(FragmentationBug.class);
		Alphabet<TlsInput> alphabet = task.getSpecification().getInputAlphabet();
		for (TlsInput input : alphabet) {
			Stream<FragmentationBug> bugStream = fragBugs.stream().filter(bug -> bug.getFragmentingInput().getInput().equals(input));
			List<FastMealyState<TlsOutput>> unsupportedStates = bugStream.map(s -> s.getState()).distinct().collect(Collectors.toList());
			if (unsupportedStates.size() / task.getSpecification().getStates().size() >= config.getUnsupportedStateRatio()) {
				
			}
		}
		return null;
	}
	
	
	static class FragmentationSupport {
		private static MutatingTlsInput mutatingInput;
		private static TlsInput input;
		private List<FastMealyState<TlsOutput>> unsupportedStates;
		

		public FragmentationSupport(List<FastMealyState<TlsOutput>> unsupportedStates) {
			super();
			this.unsupportedStates = unsupportedStates;
		}
		
		
		public static MutatingTlsInput getMutatingInput() {
			return mutatingInput;
		}
		public static TlsInput getInput() {
			return input;
		}
		public List<FastMealyState<TlsOutput>> getUnsupportedStates() {
			return unsupportedStates;
		}
	}
	
}
