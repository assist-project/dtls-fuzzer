package se.uu.it.modeltester.mutate;

import se.uu.it.modeltester.mutate.fragment.*;

/**
 * Each mutation type is associated with exactly one implementing class.
 */
public enum MutationType {
	FRAGMENT_REORDERING(ReorderingMutation.class),
	MESSAGE_SPLITTING(SplittingMutation.class),
	EMPTY_FRAGMENT_ADDITION(EmptyFragmentAdditionMutation.class);
	
	private Class<? extends Mutation<?>> mutationClass;
	
	private MutationType(Class<? extends Mutation<?>> mutationClass) {
		this.mutationClass = mutationClass;
	}
	
	public Class<? extends Mutation<?>> getMutationClass() {
		return mutationClass;
	}
}
