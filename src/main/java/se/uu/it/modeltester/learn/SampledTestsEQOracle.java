package se.uu.it.modeltester.learn;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Word;

public class SampledTestsEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
		implements
			EquivalenceOracle<A, I, D> {

	private List<Word<I>> tests;
	private MembershipOracle<I, D> sulOracle;

	public SampledTestsEQOracle(List<Word<I>> tests,
			MembershipOracle<I, D> sulOracle) {
		this.tests = tests;
		this.sulOracle = sulOracle;
	}

	@Override
	public DefaultQuery<I, D> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		for (Word<I> test : tests) {
			DefaultQuery<I, D> query = new DefaultQuery<>(test);
			D hypOutput = hypothesis.computeOutput(test);
			sulOracle.processQueries(Collections.singleton(query));
			System.out.println("SAMPLED TEST RESULT:");
			System.out.println(query);
			if (!Objects.equals(hypOutput, query.getOutput()))
				return query;
		}
		return null;
	}

}
