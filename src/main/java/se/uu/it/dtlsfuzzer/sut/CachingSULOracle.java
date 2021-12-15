package se.uu.it.dtlsfuzzer.sut;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

/**
 * This class is adapted from {@link SULOracle}. Unfortunately, the
 * implementation of LearnLib's cache oracle {@link SULCache} is unstable (the
 * version 0.12.0 at least).
 * 
 * The implementation adds terminating outputs functionality.
 */
public class CachingSULOracle<I, O> implements MealyMembershipOracle<I, O> {

	private static final Logger LOGGER = LogManager
			.getLogger(CachingSULOracle.class);

	private ObservationTree<I, O> root;

	private MembershipOracle<I, Word<O>> sulOracle;

	private boolean onlyLookup;

	private HashSet<O> terminatingOutputs;

	@SafeVarargs
	public CachingSULOracle(MembershipOracle<I, Word<O>> sulOracle,
			ObservationTree<I, O> cache, boolean onlyLookup,
			O... terminatingOutputs) {
		this.root = cache;
		this.sulOracle = sulOracle;
		this.onlyLookup = onlyLookup;
		this.terminatingOutputs = Sets.newHashSet(terminatingOutputs);
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		for (Query<I, Word<O>> q : queries) {
			Word<I> fullInput = q.getPrefix().concat(q.getSuffix());
			Word<O> fullOutput = answerFromCache(fullInput);
			if (fullOutput == null) {
				fullOutput = sulOracle.answerQuery(fullInput);
				if (!onlyLookup)
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
		if (terminatingOutputs.isEmpty())
			return root.answerQuery(input);
		else {
			Word<O> output = root.answerQuery(input, true);
			if (output.length() < input.length()) {
				if (output.isEmpty()) {
					return null;
				} else {
					if (terminatingOutputs.contains(output.lastSymbol())) {
						Word<O> extendedOutput = output;
						while (extendedOutput.length() < input.length()) {
							extendedOutput = extendedOutput.append(output
									.lastSymbol());
						}
						return extendedOutput;
					} else {
						return null;
					}
				}
			} else {
				return output;
			}
		}

	}

	public Word<O> answerQueryWithoutCache(Word<I> input) {
		return sulOracle.answerQuery(input);
	}
}
