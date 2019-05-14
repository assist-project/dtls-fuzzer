package se.uu.it.modeltester.execute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.Mutator;
import se.uu.it.modeltester.mutate.MutatorType;

/**
 * A mutating input executor applies mutators when sending a message
 * at different points. These are:
 * (1) when a message is split into fragments (in the case of DTLS);
 * (2) when a message is packed into records.
 * <br/>
 * Mutators at each point are called to generate mutations, which are
 * applied in a chained fashion on the fragmentation/packing result.
 * The mutations generated/applied for the last execution can be retrieved.
 */
public class MutatingInputExecutor extends ConcreteInputExecutor {
	private static final Logger LOGGER = LogManager.getLogger(MutatingInputExecutor.class);
	private Map<MutatorType, List<Mutator<?>>> mutators;
	
	private List<Mutation<FragmentationResult>> fragmentationMutations;
	private List<Mutation<PackingResult>> packingMutations;
	
	private MutatingInputExecutor() {
		super();
		mutators = new LinkedHashMap<>();
		fragmentationMutations = Collections.emptyList();
		packingMutations = Collections.emptyList();
	}
	
	public MutatingInputExecutor(List<Mutator<?>> mutators) {
		this();
		for (Mutator<?> mutator : mutators) {
			addMutator(mutator);
		}
	}
	

	@Override
	protected void sendMessage(ProtocolMessage message, State state) {
		ExecuteInputHelper helper = new ExecuteInputHelper();
		helper.prepareMessage(message, state);
		List<ProtocolMessage> messagesToSend = new LinkedList<>();

		if (message.isHandshakeMessage() && 
				state.getTlsContext().getConfig().getDefaultSelectedProtocolVersion().isDTLS()) {
			FragmentationResult result = helper.fragmentMessage((HandshakeMessage) message, state);
			MutatorApplicationResult<FragmentationResult> fragmentationMutationResult 
			= applyMutators(MutatorType.FRAGMENTATION, result, state);
			fragmentationMutations = fragmentationMutationResult.getAppliedMutations();
			result = fragmentationMutationResult.getResult();
			messagesToSend.addAll(result.getFragments());
		} else {
			messagesToSend.add(message);
		}
		
		PackingResult result = helper.packMessages(messagesToSend, state);
		message.getHandler(state.getTlsContext()).adjustTlsContextAfterSerialize(message);
		MutatorApplicationResult<PackingResult> packingMutationResult = applyMutators(MutatorType.PACKING, result, state);
		packingMutations = packingMutationResult.getAppliedMutations();
		result = packingMutationResult.getResult();
		helper.sendRecords(result.getRecords(), state);
	}
	
	private <R> MutatorApplicationResult<R> applyMutators(MutatorType mutatorType, R currentResult, State state) {
		List<Mutator<?>> mutatorsOfType = mutators.getOrDefault(mutatorType, Collections.emptyList());
		R result = currentResult;
		List<Mutation<R>> appliedMutations = new LinkedList<>();
		for (Mutator<?> mutator : mutatorsOfType) {
			// this can definitely be made safe with some more work;
			Mutator<R> castMutator = (Mutator<R>) mutator;
			Mutation<R> mutation = castMutator.generateMutation(result, state.getTlsContext());
			result = mutation.mutate(result, state.getTlsContext());
			appliedMutations.add(mutation);
		}
		return new MutatorApplicationResult<R>(result, appliedMutations);
	}
	
	/**
	 * Adds a mutator
	 */
	private <M extends Mutator<?>> boolean addMutator(M mutator) {
		mutators.putIfAbsent(mutator.getType(), new LinkedList<>());
		List<Mutator<?>> mutOfSameType = mutators.get(mutator.getType());
		mutOfSameType.add(mutator);
		return true;
	}
	
	public List<Mutation<?>> getLastAppliedMutations() {
		return Stream.concat(fragmentationMutations.stream(), packingMutations.stream())
				.collect(Collectors.toList());
	}
	
	public String getCompactMutatorDescription() {
		return mutators.values().stream()
				.flatMap(l -> l.stream()).map(m -> m.toString())
				.reduce((s1,s2) -> s1 + "_" + s2).get();
	}
	
	static class MutatorApplicationResult<R> {
		private R result;
		private List<Mutation<R>> appliedMutations;
		MutatorApplicationResult(R result, List<Mutation<R>> appliedMutations) {
			this.result = result;
			this.appliedMutations = appliedMutations;
		}
		public R getResult() {
			return result;
		}
		public List<Mutation<R>> getAppliedMutations() {
			return appliedMutations;
		}
	}
	
}
