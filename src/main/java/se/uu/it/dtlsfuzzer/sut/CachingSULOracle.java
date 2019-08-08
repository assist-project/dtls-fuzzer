package se.uu.it.dtlsfuzzer.sut;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.cache.sul.SULCache;
import de.learnlib.oracles.SULOracle;
import net.automatalib.words.Word;

/**
 * This class is adapted from {@link SULOracle}. Unfortunately, the
 * implementation of LearnLib's cache oracle {@link SULCache} is unstable.
 */
public class CachingSULOracle<I, O> implements MealyMembershipOracle<I, O> {

	private static final Logger LOGGER = LogManager
			.getLogger(CachingSULOracle.class);

	private ObservationTree<I, O> root;

	private MembershipOracle<I, Word<O>> sulOracle;

	public CachingSULOracle(MembershipOracle<I, Word<O>> sulOracle,
			ObservationTree<I, O> cache) {
		this.root = cache;
		this.sulOracle = sulOracle;
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		for (Query<I, Word<O>> q : queries) {
			Word<I> fullInput = q.getPrefix().concat(q.getSuffix());
			Word<O> fullOutput = answerFromCache(fullInput);
			if (fullOutput == null) {
				fullOutput = sulOracle.answerQuery(fullInput);
				storeToCache(fullInput, fullOutput);
			} else {
				LOGGER.info("CACHE HIT!");
			}

			Word<O> output = fullOutput.suffix(q.getSuffix().size());
			q.answer(output);
			LOGGER.info(q.toString());
		}
	}

	private void storeToCache(Word<I> input, Word<O> output) {
		root.addObservation(input, output);
	}

	@Nullable
	private Word<O> answerFromCache(Word<I> input) {
		return root.answerQuery(input);
	}
	
	public Word<O> answerQueryWithoutCache(Word<I> input) {
		return sulOracle.answerQuery(input);
	}
}
