package se.uu.it.dtlsfuzzer;

import java.io.File;
import java.io.IOException;

import net.automatalib.words.Alphabet;
import se.uu.it.dtlsfuzzer.config.StateFuzzerConfig;
import se.uu.it.dtlsfuzzer.learner.Learner;
import se.uu.it.dtlsfuzzer.learner.Learner.LearnerResult;
import se.uu.it.dtlsfuzzer.sut.input.AlphabetFactory;
import se.uu.it.dtlsfuzzer.sut.input.TlsInput;

public class StateFuzzer {
	private StateFuzzerConfig config;
	private CleanupTasks cleanupTasks;

	public StateFuzzer(StateFuzzerConfig config) {
		this.config = config;
	}

	public void startFuzzing() throws IOException {
		this.cleanupTasks = new CleanupTasks();
		try {
				// setting up our output directory
				File folder = new File(config.getOutput());
				folder.mkdirs();
				extractModel(config);
		} catch (Exception e) {
			cleanupTasks.execute();
			throw e;
		}
		cleanupTasks.execute();
	}
	
	private LearnerResult extractModel(StateFuzzerConfig config) {
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
		Learner learner = new Learner(config, alphabet, cleanupTasks);
		LearnerResult result = learner.inferStateMachine();
		return result;
	}
}
