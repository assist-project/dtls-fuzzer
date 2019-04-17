package de.rub.nds.modelfuzzer.sut.io;

import de.rub.nds.modelfuzzer.fuzz.DtlsMessageFragmenter;
import de.rub.nds.modelfuzzer.fuzz.FragmentationGeneratorFactory;
import de.rub.nds.modelfuzzer.fuzz.FragmentationStrategy;
import de.rub.nds.modelfuzzer.fuzz.FragmentingInputExecutor;

public class FragmentedTlsInput extends FuzzedTlsInput{
	
	private int numFragments;
	private FragmentationStrategy strategy;

	private static FragmentingInputExecutor buildFragmenter(int numFragments, FragmentationStrategy strategy) {
		DtlsMessageFragmenter fragmenter = new DtlsMessageFragmenter(numFragments);
		FragmentingInputExecutor fragmentingExecutor = new FragmentingInputExecutor(fragmenter, 
				FragmentationGeneratorFactory.buildGenerator(strategy));
		return fragmentingExecutor;
	}

	public FragmentedTlsInput(TlsInput input, int numFragments, FragmentationStrategy strategy) {
		super(input, buildFragmenter(numFragments, strategy));
		this.numFragments = numFragments;
		this.strategy = strategy;
	}

	@Override
	public String toString() {
		return "FRAG_"+strategy.name()+"_"+numFragments+"_"+super.getInput().toString();
	}

}
