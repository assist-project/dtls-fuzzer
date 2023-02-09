package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
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
		writer.flush();
	}
}
