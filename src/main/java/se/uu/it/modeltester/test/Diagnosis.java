package se.uu.it.modeltester.test;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.uu.it.modeltester.mutate.Mutator;
import se.uu.it.modeltester.sut.io.TlsInput;

// TODO A nicer design is to make classes for each type of problem, with these classes subclassing an abstract DiagnosisItem class.
public class Diagnosis {
	private List<TlsInput> fragmentationGloballyNotSupported;
	private Map<TlsInput, List<?>> fragmentationNotSupportedInStates;
	private List<List<Mutator<?>>> mutatorCombinationGloballyNotSupported;
	
	
	public Diagnosis() {
		fragmentationNotSupportedInStates = new LinkedHashMap<>();
		fragmentationGloballyNotSupported = new LinkedList<>();
		mutatorCombinationGloballyNotSupported = new LinkedList<>();
	}
	
	public void addFragmentationGloballyNotSupported(TlsInput input) {
		fragmentationGloballyNotSupported.add(input);
	}
	
	public void addFragmentationNotSupportInStates(TlsInput input, List<?> states) {
		fragmentationNotSupportedInStates.put(input, states);
	}
	
	public void addMutatorCombinationGloballyUnsupported(List<Mutator<?>> mutators) {
		
	}
	
	
	public void printDiagnosis(PrintStream ps) {
		ps.println("Diagnosis");
		ps.println("");
		if (!fragmentationGloballyNotSupported.isEmpty()) {
			ps.println("Fragmentation is not supported globally for the following inputs");
			fragmentationGloballyNotSupported.forEach(i -> ps.println(i.toString()));
		} 
		if (!fragmentationNotSupportedInStates.isEmpty()) {
			ps.println("Fragmentation is not supported for the following inputs");
		}
		
	}
}
