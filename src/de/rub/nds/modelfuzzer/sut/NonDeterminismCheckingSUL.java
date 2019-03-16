package de.rub.nds.modelfuzzer.sut;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;

/**
 * <pre>Copied from <a href="https://gitlab.science.ru.nl/ramonjanssen/basic-learning/">basic-learning</a>.
 * </pre>
 * 
 * SUL-wrapper to check for non-determinism, by use of an observation tree.
 * 
 * @author Ramon Janssen
 *
 * @param <I>
 * @param <O>
 */
public class NonDeterminismCheckingSUL<I,O> implements SUL<I,O> {
	private final SUL<I,O> sul;
	private final ObservationTree<I,O> root = new ObservationTree<I,O>();
	private final List<I> inputs = new ArrayList<>();
	private final List<O> outputs = new ArrayList<>();
	
	public NonDeterminismCheckingSUL(SUL<I,O> sul) {
		this.sul = sul;
	}

	@Override
	public void post() {
		sul.post();
		// check for non-determinism: crashes if outputs are inconsistent with previous ones
		root.addObservation(inputs, outputs);
		inputs.clear();
		outputs.clear();
	}

	@Override
	public void pre() {
		sul.pre();
	}

	@Override
	public O step(I input) throws SULException {
		O result = sul.step(input);
		inputs.add(input);
		outputs.add(result);
		return result;
	}
}
