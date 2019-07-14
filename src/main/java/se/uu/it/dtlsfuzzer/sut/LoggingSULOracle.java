package se.uu.it.dtlsfuzzer.sut;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.Query;
import net.automatalib.words.Word;

public class LoggingSULOracle<I, O> implements MealyMembershipOracle<I, O> {
	private MealyMembershipOracle<I, O> oracle;
	private PrintWriter writer;

	public LoggingSULOracle(MealyMembershipOracle<I, O> oracle, Writer writer) {
		this.oracle = oracle;
		this.writer = new PrintWriter(writer);
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
		oracle.processQueries(queries);
		queries.forEach(q -> writer.println(q.toString()));
	}
}
