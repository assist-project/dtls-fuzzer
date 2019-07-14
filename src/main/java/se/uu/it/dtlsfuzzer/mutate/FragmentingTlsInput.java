package se.uu.it.dtlsfuzzer.mutate;

import java.util.List;
import java.util.stream.Collectors;

import se.uu.it.dtlsfuzzer.sut.io.TlsInput;

public class FragmentingTlsInput extends MutatingTlsInput {

	public FragmentingTlsInput(TlsInput input,
			List<FragmentationMutator> mutators) {
		super(input, mutators.stream().collect(Collectors.toList()));
	}
}
