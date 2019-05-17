package se.uu.it.modeltester.execute;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.mutate.Mutation;

/**
 * A mutated input executor applies mutations at different stages in
 * sending an input. These stages coincide with those of the {@link MutatingInputExecutor}.
 * 
 * It server as a "replayer" for already generated mutations which produced 
 * interesting results.
 */
public class MutatedInputExecutor extends TestingInputExecutor{
	
	private List<Mutation<FragmentationResult>> fragmentationMutations;
	private List<Mutation<PackingResult>> packingMutations;
	
	public MutatedInputExecutor(List<Mutation<?>> mutations) {
		// TODO bad design, we should at least make Mutated and Mutating Input Executor implementations be consistent
		fragmentationMutations = new LinkedList<>();
		packingMutations = new LinkedList<>();
		
		
		for (Mutation<?> mutation : mutations) {
			if (mutation.getType().isFragmenting()) {
				fragmentationMutations.add((Mutation<FragmentationResult>) mutation);
			} 
			if (mutation.getType().isPacking()) {
				packingMutations.add((Mutation<PackingResult>) mutation);
			}
		}
	}

	
	protected FragmentationResult fragmentMessage(ProtocolMessage message, State state, ExecutionContext context) {
		FragmentationResult result = super.fragmentMessage(message, state, context);
		result = applyMutations(fragmentationMutations, result, state);
		return result;
	}
	
	protected PackingResult packMessages(List<ProtocolMessage> messagesToSend, State state, ExecutionContext context) {
		PackingResult result = super.packMessages(messagesToSend, state, context);
		result = applyMutations(packingMutations, result, state);
		return result;
	}
	
	private <R> R applyMutations(List<Mutation<R>> mutations, R result, State state)  {
		R mutatedResult = result;
		for (Mutation<R> mutation : mutations) {
			mutatedResult = mutation.mutate(result, state.getTlsContext(), null);
		}
		return mutatedResult; 
	}
	
	public List<Mutation<?>> getMutations() {
		return Stream.concat(fragmentationMutations.stream(), packingMutations.stream())
				.collect(Collectors.toList());
	}
	
	public String getCompactMutationDescription() {
		return Stream.concat(fragmentationMutations.stream(), packingMutations.stream())
				.map(m -> m.toString())
				.reduce((s1,s2) -> s1 + "_" + s2).orElse("");
	}
}
