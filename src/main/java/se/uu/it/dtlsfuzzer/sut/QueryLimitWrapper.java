package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;

public class QueryLimitWrapper <I, O> implements SUL<I, O> {

	private SUL<I, O> sul;
	private final long limit;
	private long numQueries;

	public QueryLimitWrapper(SUL<I,O> sul, long limit) {
		this.sul = sul;
		this.limit = limit;
	}
	
	@Override
	public void pre() {
		sul.pre();
	}

	@Override
	public void post() {
		sul.post();
		numQueries ++;
		if (numQueries == limit) {
			throw new QueryLimitReachedException(limit);
		}
	}

	@Override
	public O step(I in) {
		return sul.step(in);
	}

}
