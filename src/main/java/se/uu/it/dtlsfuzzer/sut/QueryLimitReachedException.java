package se.uu.it.dtlsfuzzer.sut;

public class QueryLimitReachedException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	private long queryLimit;
	
	QueryLimitReachedException(long queryLimit) { 
		super("Experiment has exceeded the duration limit given: "
				+ queryLimit);
		
		this.queryLimit = queryLimit;
	}
	
	public long getQueryLimit() {
		return queryLimit;
	}
	
}
