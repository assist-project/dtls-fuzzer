package se.uu.it.dtlsfuzzer.sut;

import java.time.Duration;

import de.learnlib.api.SUL;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import se.uu.it.dtlsfuzzer.CleanupTasks;
import se.uu.it.dtlsfuzzer.config.MapperConfig;
import se.uu.it.dtlsfuzzer.config.SulDelegate;
import se.uu.it.dtlsfuzzer.mapper.AbstractMapper;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.OutputMapper;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class TlsSULBuilder {
	private TlsSUL tlsSul;
	private SUL<TlsInput, TlsOutput> wrappedTLSSul;
	private Counter inputCounter;
	private Counter resetCounter;
	private Duration timeLimit;
	private Long queryLimit;
	
	public TlsSULBuilder(SulDelegate delegate, MapperConfig mapperConfig, AbstractMapper defaultExecutor, 
			CleanupTasks cleanupTasks) {
		tlsSul = new TlsSUL(delegate, mapperConfig, defaultExecutor, cleanupTasks);
		SUL<TlsInput, TlsOutput> tlsSystemUnderTest = tlsSul;

		if (delegate.getCommand() != null) {
			tlsSystemUnderTest = new TlsProcessWrapper(tlsSystemUnderTest,
					delegate);
		}
		
		if (delegate.getResetPort() != null) {
			if (delegate.getRole().equals("server")) {
				ResettingClientWrapper<TlsInput, TlsOutput> resetWrapper = new ResettingClientWrapper<TlsInput, TlsOutput>(
						tlsSystemUnderTest, delegate, cleanupTasks);
				tlsSystemUnderTest = resetWrapper;
			}
			else {
				ResettingWrapper<TlsInput, TlsOutput> resetWrapper = new ResettingWrapper<TlsInput, TlsOutput>(
						tlsSystemUnderTest, delegate,
						cleanupTasks);
				tlsSul.setDynamicPortProvider(resetWrapper);
				tlsSystemUnderTest = resetWrapper;
			}
		}
		
		tlsSystemUnderTest = new IsAliveWrapper(tlsSystemUnderTest, new OutputMapper(mapperConfig));
		
//		if (!delegate.isClient()) {
//			tlsSystemUnderTest = new ClientConnectWrapper(tlsSystemUnderTest);
//		}

		SymbolCounterSUL<TlsInput, TlsOutput> symbolCounterSul = new SymbolCounterSUL<>(
				"symbol counter", tlsSystemUnderTest);
		ResetCounterSUL<TlsInput, TlsOutput> resetCounterSul = new ResetCounterSUL<>(
				"reset counter", symbolCounterSul);
		inputCounter = symbolCounterSul.getStatisticalData();
		resetCounter = resetCounterSul.getStatisticalData();
		
		wrappedTLSSul = resetCounterSul;
	}
	
	public TlsSULBuilder setTimeLimit(Duration timeLimit) {
		if (this.timeLimit == null) {
			this.timeLimit = timeLimit;
			wrappedTLSSul = new TimeoutWrapper<TlsInput, TlsOutput>(
					wrappedTLSSul, timeLimit);
			return this;
		} else {
			throw new RuntimeException("Time limit already set to " + timeLimit.toString());
		}
	}
	
	public TlsSULBuilder setQueryLimit(long queryLimit) {
		if (this.queryLimit == null) {
			this.queryLimit = queryLimit;
			wrappedTLSSul = new QueryLimitWrapper<TlsInput, TlsOutput>(
					wrappedTLSSul, queryLimit);
			return this;
		} else {
			throw new RuntimeException("Query limit already set to " + queryLimit);
		}
		
	}
	
	public TlsSUL getTLSSul() {
		return tlsSul;
	}
	
	public SUL<TlsInput,TlsOutput> getWrappedTlsSUL() {
		return wrappedTLSSul;
	}
	
	public Counter getInputCounter() {
		return inputCounter;
	}
	
	public Counter getResetCounter() {
		return resetCounter;
	}
}
