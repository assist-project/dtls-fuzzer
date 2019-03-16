package de.rub.nds.modelfuzzer.sut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.automatalib.words.Word;

/**
 * <pre>Copied from <a href="https://gitlab.science.ru.nl/ramonjanssen/basic-learning/">basic-learning</a>.
 * </pre>
 * 
 * @author Ramon Janssen
 *
 * @param <I> the input type of the observations
 * @param <O> the output type of the observations
 */
public class ObservationTree<I,O> {	
	private final ObservationTree<I,O> parent;
	private final I parentInput;
	private final O parentOutput;
	private final Map<I, ObservationTree<I,O>> children;
	private final Map<I, O> outputs;
	
	public ObservationTree() {
		this(null, null, null);
	}
	
	private ObservationTree(ObservationTree<I,O> parent, I parentInput, O parentOutput) {
		this.children = new HashMap<>();
		this.outputs = new HashMap<>();
		this.parent = parent;
		this.parentInput = parentInput;
		this.parentOutput = parentOutput;
	}
	
	/**
	 * @return The outputs observed from the root of the tree until this node
	 */
	private List<O> getOutputChain() {
		if (this.parent == null) {
			return new LinkedList<O>();
		} else {
			List<O> parentChain = this.parent.getOutputChain();
			parentChain.add(parentOutput);
			return parentChain;
		}
	}
	
	private List<I> getInputChain() {
		if (this.parent == null) {
			return new LinkedList<I>();
		} else {
			List<I> parentChain = this.parent.getInputChain();
			parentChain.add(this.parentInput);
			return parentChain;
		}
	}

	/**
	 * Add one input and output symbol and traverse the tree to the next node
	 * @param input
	 * @param output
	 * @return the next node
	 * @throws InconsistencyException 
	 */
	public ObservationTree<I,O> addObservation(I input, O output) throws CacheInconsistencyException {
		O previousOutput = this.outputs.get(input);
		boolean createNewBranch = previousOutput == null;
		if (createNewBranch) {
			// input hasn't been queried before, make a new branch for it and traverse
			this.outputs.put(input, output);
			ObservationTree<I,O> child = new ObservationTree<I,O>(this, input, output);
			this.children.put(input, child);
			return child;
		} else if (!previousOutput.equals(output)) {
			// input is inconsistent with previous observations, throw exception
			List<O> oldOutputChain = this.children.get(input).getOutputChain();
			List<O> newOutputChain = this.getOutputChain();
			List<I> inputChain = this.getInputChain();
			newOutputChain.add(output);
			throw new CacheInconsistencyException(toWord(inputChain), toWord(oldOutputChain), toWord(newOutputChain));
		} else {
			// input is consistent with previous observations, just traverse
			return this.children.get(input);
		}
	}

	/**
	 * Add Observation to the tree
	 * @param inputs
	 * @param outputs
	 * @throws CacheInconsistencyException Inconsistency between new and stored observations
	 */
	public void addObservation(Word<I> inputs, Word<O> outputs) throws CacheInconsistencyException {
		addObservation(inputs.asList(), outputs.asList());
	}
	
	
	public void addObservation(List<I> inputs, List<O> outputs) throws CacheInconsistencyException {
		if (inputs.isEmpty() && outputs.isEmpty()) {
			return;
		} else if (inputs.isEmpty() || outputs.isEmpty()) {
			throw new RuntimeException("Input and output words should have the same length:\n" + inputs + "\n" + outputs);
		} else {
			I firstInput = inputs.get(0);
			O firstOutput = outputs.get(0);
			try {
				this.addObservation(firstInput, firstOutput)
					.addObservation(inputs.subList(1, inputs.size()), outputs.subList(1, outputs.size()));
			} catch (CacheInconsistencyException e) {
				throw new CacheInconsistencyException(toWord(inputs), e.getOldOutput(), toWord(outputs));
			}
		}
	}
	
	@Nullable
	public Word<O> answerQuery(Word<I> word) {
		List<I> inputChain = word.asList();
		List<O> outputChain = answerInputChain(inputChain);
		if (outputChain != null) {
			return toWord(outputChain);
		}
		return null;
	}
	
	@Nullable
	public List<O> answerInputChain(List<I> inputs) {
		if (inputs.isEmpty())
			return Collections.emptyList();
		else {
			I input = inputs.get(0);
			O output = outputs.get(input);
			ObservationTree<I, O> nextObservationTree = this.children.get(input);
			if (output == null || nextObservationTree == null) {
				return null;
			} else {
				List<I> nextInputs = inputs.subList(1, inputs.size());
				List<O> nextOutputs = nextObservationTree.answerInputChain(nextInputs);
				if (nextOutputs == null) {
					return null;
				}
				else {
					List<O> outputs = new ArrayList<>(inputs.size());
					outputs.add(output);
					outputs.addAll(nextOutputs);
					return outputs;
				}
			}
		}
	}
	
	public static<T> Word<T> toWord(List<T> symbolList) {
		return Word.fromList(symbolList);
	}
}
