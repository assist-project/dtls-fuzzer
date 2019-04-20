package se.uu.it.modeltester.fuzz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class FragmentationGeneratorFactory {
	public static FragmentationGenerator buildGenerator(FragmentationStrategy strategy) {
		switch (strategy) {
		case EVEN:
			return new EvenFragmentationGenerator();
		case RANDOM:
			return new RandomFragmentationGenerator();
		default:
			throw new RuntimeException("Not supported");
		}
	}
	
	static class EvenFragmentationGenerator implements FragmentationGenerator{

		public Fragmentation generateFragmentation(int numFragments, int length) {
			if (numFragments <= 0) {
				throw new RuntimeException("Invalid number of fragments");
			} else {
				List<Fragment> fragments = new ArrayList<Fragment>();
				int step = Math.max((int)Math.ceil(length/numFragments), 1);
				
		    	for (int i=0; i<length; i+=step) {
		    		int fragLen = i+step > length ? length-i : step;
		    		fragments.add(new Fragment(i, fragLen));
		    	}
		    	
		    	assert(fragments.get(fragments.size()-1).getLength() + fragments.get(fragments.size()-1).getOffset() == length);
		    	
		    	// fill rest with 0
		    	while (fragments.size() < numFragments) {
		    		fragments.add(new Fragment(length, 0));
		    	}
		    	
				return new Fragmentation(fragments);
			}
		}
		
	}
	
	static class RandomFragmentationGenerator implements FragmentationGenerator{

		public Fragmentation generateFragmentation(int numFragments, int length) {
			List<Fragment> fragments = new ArrayList<Fragment>(numFragments);
			final Random rand = new Random(0);
			int[]  offsets = IntStream.concat( 
					IntStream.generate(() -> rand.nextInt(length+1)).limit(numFragments-1), 
					IntStream.of(length))
					.toArray();
			Arrays.sort(offsets);
			int crtOffset = 0;
			for (int offset : offsets) {
				fragments.add(new Fragment(crtOffset, offset - crtOffset));
				crtOffset = offset;
			}
			
			return new Fragmentation(fragments);
		}
		
	}
}
