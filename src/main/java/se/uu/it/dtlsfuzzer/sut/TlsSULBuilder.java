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
	private Counter testCounter;
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

		if (delegate.getSulAdapterConfig().getAdapterPort() != null) {
			TlsSULAdapterWrapper adapterWrapper = new TlsSULAdapterWrapper(
					tlsSystemUnderTest, delegate.getSulAdapterConfig(), !delegate.isClient(), cleanupTasks);
			tlsSystemUnderTest = adapterWrapper;
			tlsSul.setDynamicPortProvider(adapterWrapper);
		}

		tlsSystemUnderTest = new IsAliveWrapper(tlsSystemUnderTest, new OutputMapper(mapperConfig));


		SymbolCounterSUL<TlsInput, TlsOutput> symbolCounterSul = new SymbolCounterSUL<>(
				"symbol counter", tlsSystemUnderTest);
		ResetCounterSUL<TlsInput, TlsOutput> testCounterSul = new ResetCounterSUL<>(
				"test counter", symbolCounterSul);
		inputCounter = symbolCounterSul.getStatisticalData();
		testCounter = testCounterSul.getStatisticalData();

		wrappedTLSSul = testCounterSul;
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

	public TlsSULBuilder setTestLimit(long queryLimit) {
		if (this.queryLimit == null) {
			this.queryLimit = queryLimit;
			wrappedTLSSul = new TestLimitWrapper<TlsInput, TlsOutput>(
					wrappedTLSSul, queryLimit);
			return this;
		} else {
			throw new RuntimeException("Test limit already set to " + queryLimit);
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

	public Counter getTestCounter() {
		return testCounter;
	}
}
