package se.uu.it.dtlsfuzzer.learner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Word;

public class SampledTestsEQOracle<I,O>
		implements
		EquivalenceOracle.MealyEquivalenceOracle<I, O> {

	private List<Word<I>> tests;
	private MealyMembershipOracle<I, O> sulOracle;

	public SampledTestsEQOracle(List<Word<I>> tests,
			MealyMembershipOracle<I, O> sulOracle) {
		this.tests = tests;
		this.sulOracle = sulOracle;
	}

	@Override
	public @Nullable DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
			Collection<? extends I> inputs) {
		for (Word<I> test : tests) {
			DefaultQuery<I, Word<O>> query = new DefaultQuery<>(test);
			Word<O> hypOutput = hypothesis.computeOutput(test);
			sulOracle.processQueries(Collections.singleton(query));
			if (!Objects.equals(hypOutput, query.getOutput()))
				return query;
		}
		return null;
	}

}
