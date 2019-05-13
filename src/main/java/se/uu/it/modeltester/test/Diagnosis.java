package se.uu.it.modeltester.test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.uu.it.modeltester.sut.io.TlsInput;

public class Diagnosis {
	private List<TlsInput> fragmentationGloballyNotSupported;
	private Map<TlsInput, List<Object>> fragmentationNotSupportedInStates;
	
	public Diagnosis() {
		fragmentationNotSupportedInStates = new LinkedHashMap<>();
		fragmentationGloballyNotSupported = new LinkedList<>();
	}
	
}
