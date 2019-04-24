package se.uu.it.modeltester.sut.io;

import se.uu.it.modeltester.mutate.HandshakeMessageFragmenter;
import se.uu.it.modeltester.mutate.MutatedTlsInput;
import se.uu.it.modeltester.mutate.FragmentationGeneratorFactory;
import se.uu.it.modeltester.mutate.FragmentationStrategy;
import se.uu.it.modeltester.test.FragmentingInputExecutor;

public class FragmentedTlsInput extends MutatedTlsInput{
	
	public FragmentedTlsInput(TlsInput input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

	private int numFragments;
	private FragmentationStrategy strategy;

	private static FragmentingInputExecutor buildFragmenter(int numFragments, FragmentationStrategy strategy) {
		HandshakeMessageFragmenter fragmenter = new HandshakeMessageFragmenter(numFragments);
		FragmentingInputExecutor fragmentingExecutor = new FragmentingInputExecutor(fragmenter, 
				FragmentationGeneratorFactory.buildGenerator(strategy));
		return fragmentingExecutor;
	}

//	public FragmentedTlsInput(TlsInput input, int numFragments, FragmentationStrategy strategy) {
//		super(input, buildFragmenter(numFragments, strategy));
//		this.numFragments = numFragments;
//		this.strategy = strategy;
//	}
	
	@Override
	public String toString() {
		return "FRAG_"+strategy.name()+"_"+numFragments+"_"+super.getInput().toString();
	}

}
