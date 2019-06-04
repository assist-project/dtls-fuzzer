package se.uu.it.modeltester.mutate;

import se.uu.it.modeltester.mutate.fragment.*;
import se.uu.it.modeltester.mutate.record.*;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.execute.PackingResult;

/**
 * Each mutation type is associated with exactly one implementing class.
 */
public enum MutationType {
	FRAGMENT_REORDERING(ReorderingMutation.class, FragmentationResult.class),
	MESSAGE_SPLITTING(SplittingMutation.class, FragmentationResult.class),
	EMPTY_FRAGMENT_ADDITION(EmptyFragmentAdditionMutation.class, FragmentationResult.class),
	RECORD_REORDERING(RecordSwapMutation.class, PackingResult.class),
	;
	
	private Class<? extends Mutation<?>> mutationClass;
	private Class<?> resultType;
	
	private <T> MutationType(Class<? extends Mutation<T>> mutationClass, Class<T> resultType) {
		this.mutationClass = mutationClass;
		this.resultType = resultType;
	}
	
	public Class<? extends Mutation<?>> getMutationClass() {
		return mutationClass;
	}
	
	public Class<?> getResultType() {
		return resultType;
	}
	
	public boolean isFragmenting() {
		return resultType == FragmentationResult.class;
	}
	
	public boolean isPacking() {
		return resultType == PackingResult.class;
	}
}
