package se.uu.it.dtlsfuzzer.config;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.automatalib.words.Alphabet;
import se.uu.it.dtlsfuzzer.CleanupTasks;
import se.uu.it.dtlsfuzzer.TestRunner;
import se.uu.it.dtlsfuzzer.TestRunnerResult;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;
import se.uu.it.dtlsfuzzer.sut.output.TlsOutput;

public class ProbeTestRunner extends TestRunner {

	private StateFuzzerConfig config;
	private Alphabet<TlsInput> alphabet;
	private List<TestRunnerResult<TlsInput, TlsOutput>> control = null;

	public ProbeTestRunner(TestRunnerConfig config, Alphabet<TlsInput> alphabet, SulDelegate sulDelegate, MapperConfig mapperConfig, CleanupTasks cleanupTasks) throws IOException {
		super(config, alphabet, sulDelegate, mapperConfig, cleanupTasks);
	}

	public boolean isNonDeterministic(boolean controlRun) throws IOException {
		List<TestRunnerResult<TlsInput, TlsOutput>> results = super.runTests();
		Iterator<TestRunnerResult<TlsInput, TlsOutput>> itControl = null;
		if (!controlRun) {
			itControl = control.iterator();
		}
		for (TestRunnerResult<TlsInput, TlsOutput> result : results) {
			if (result.getGeneratedOutputs().size() > 1) {
				return true;
			}
			if  (itControl != null && !(result.getGeneratedOutputs().equals(itControl.next().getGeneratedOutputs()))) {
				return true;
			}
		}
		if (controlRun) {
			control = results;
		}
		return false;
	}

	public StateFuzzerConfig getConfig() {
		return config;
	}

	public Alphabet<TlsInput> getAlphabet() {
		return alphabet;
	}
}
