package se.uu.it.modeltester.sut.io;

import se.uu.it.modeltester.test.DtlsMessageFragmenter;
import se.uu.it.modeltester.test.FragmentationGeneratorFactory;
import se.uu.it.modeltester.test.FragmentationStrategy;
import se.uu.it.modeltester.test.FragmentingInputExecutor;

public class FragmentedTlsInput extends MutatedTlsInput{
	
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
