package se.uu.it.modeltester.execute;

import java.util.Collections;

/**
 * Builds on the MutatingInputExecutor, but applies no mutation.
 * 
 */
public final class NonMutatingInputExecutor extends MutatingInputExecutor {

	public NonMutatingInputExecutor() {
		super(Collections.emptyList());
	}

}
