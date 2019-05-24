package se.uu.it.modeltester.sut;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.SUL;
import de.learnlib.oracles.SULOracle;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * This class is adapted from {@link SULOracle}. Unfortunately, the
 * implementation of LearnLib's cache oracle {@link SULCache} is unstable.
 */
public class CachingSULOracle<I, O> implements MealyMembershipOracle<I, O> {

	private static final Logger LOG = LogManager.getLogger();

	private ObservationTree<I, O> root;
	private SUL<I, O> sul;

	public CachingSULOracle(SUL<I, O> sul) {
		root = new ObservationTree<I, O>();
		this.sul = sul;
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		processQueries(sul, queries);
	}

	private void processQueries(SUL<I, O> sul,
			Collection<? extends Query<I, Word<O>>> queries) {
		for (Query<I, Word<O>> q : queries) {
			Word<I> fullInput = q.getPrefix().concat(q.getSuffix());
			Word<O> fullOutput = answerFromCache(fullInput);
			if (fullOutput == null) {
				fullOutput = answer(fullInput);
				storeToCache(fullInput, fullOutput);
			} else {
				LOG.info("CACHE HIT!");
			}

			Word<O> output = fullOutput.suffix(q.getSuffix().size());
			q.answer(output);
			LOG.info(q.toString());
		}
	}

	private void storeToCache(Word<I> input, Word<O> output) {
		root.addObservation(input, output);
	}

	@Nullable
	private Word<O> answerFromCache(Word<I> input) {
		return root.answerQuery(input);
	}

	@Nonnull
	private Word<O> answer(Word<I> input) {
		sul.pre();
		try {

			WordBuilder<O> wb = new WordBuilder<>(input.length());
			for (I sym : input) {
				wb.add(sul.step(sym));
			}

			return wb.toWord();
		} finally {
			sul.post();
		}
	}
}
