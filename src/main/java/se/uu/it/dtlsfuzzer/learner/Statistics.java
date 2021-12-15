package se.uu.it.dtlsfuzzer.learner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Alphabet;
import se.uu.it.dtlsfuzzer.config.LearningConfig;
import se.uu.it.dtlsfuzzer.config.StateFuzzerConfig;
import se.uu.it.dtlsfuzzer.config.SulDelegate;

/**
 * Statistics collected over the learning process.
 */
public class Statistics {
	private String runDescription;
	private int states;
	private long learnResets;
	private long learnInputs;
	private long allResets;
	private long allInputs;
	private List<DefaultQuery<?, ?>> counterexamples;
	private long duration;
	private long lastHypResets;
	private long lastHypInputs;
	private boolean successful;
	private List<HypothesisStatistics> hypStats;

	protected Statistics() {
		runDescription = "";
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		export(sw);
		String statsString = sw.toString();
		return statsString;
	}

	public void export(Writer writer) {
		PrintWriter out = new PrintWriter(writer);
		out.println(runDescription);
		out.println("=== STATISTICS ===");
		out.println("Learning successful: " + successful);
		out.println("Number of states: " + states);
		out.println("Number of hypotheses: " + (counterexamples.size() - 1));
		out.println("Number of inputs: " + allInputs);
		out.println("Number of resets: " + allResets);
		out.println("Number of learning inputs: " + learnInputs);
		out.println("Number of learning resets: " + learnResets);
		out.println("Number of inputs up to last hypothesis: " + lastHypInputs);
		out.println("Number of resets up to last hypothesis: " + lastHypResets);
		out.println("Time it took to learn model: " + duration);
		out.println("Counterexamples:");
		int ind = 1;
		for (Object ce : counterexamples) {
			out.println("CE " + (ind++) + ":" + ce);
		}
		if (!hypStats.isEmpty()) {
			out.println("Number of inputs when hypothesis was generated: " + hypStats.stream().map(s -> s.getSnapshot().getInputs()).collect(Collectors.toList()));
			out.println("Number of resets when hypothesis was generated: " + hypStats.stream().map(s -> s.getSnapshot().getResets()).collect(Collectors.toList()));
			out.println("Time when hypothesis was generated: " + hypStats.stream().map(s -> s.getSnapshot().getTime()).collect(Collectors.toList()));
			
			List<HypothesisStatistics> invalidatedHypStates = new ArrayList<>(hypStats);
			if (invalidatedHypStates.get(invalidatedHypStates.size()-1).getCounterexample() == null) {
				invalidatedHypStates.remove(invalidatedHypStates.size()-1);
			}
			out.println("Number of inputs when counterexample was found: " + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getInputs()).collect(Collectors.toList()));
			out.println("Number of resets when counterexample was found: " + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getResets()).collect(Collectors.toList()));
			out.println("Time when counterexample was found: " + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getTime()).collect(Collectors.toList()));
		}
		out.close();
	}

	protected void generateRunDescription(StateFuzzerConfig config,
			Alphabet<?> alphabet) {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		out.println("=== RUN DESCRIPTION ===");
		out.println("Learning Parameters");
		out.println("Alphabet: " + alphabet);
		LearningConfig learningConfig = config.getLearningConfig();
		out.println("Learning Algorithm: "
				+ learningConfig.getLearningAlgorithm());
		out.println("Equivalence Algorithms: "
				+ learningConfig.getEquivalenceAlgorithms());
		out.println("Min Length: " + learningConfig.getMinLength());
		out.println("Max Length: " + learningConfig.getMaxLength());
		out.println("Random Length: " + learningConfig.getRandLength());
		out.println("Max Depth: " + learningConfig.getMaxDepth());
		out.println("Prob Reset: " + learningConfig.getProbReset());
		out.println("Max Queries: " + learningConfig.getNumberOfQueries());
		out.println("TLS SUL Parameters");
		SulDelegate sulDelegate = config.getSulDelegate();
		out.println("Protocol: " + sulDelegate.getProtocolVersion());
		out.println("ResetWait: " + sulDelegate.getResetWait());
		out.println("Timeout: " + sulDelegate.getTimeout());
		if (sulDelegate.getCommand() != null) {
			out.println("RunWait: " + sulDelegate.getRunWait());
			out.println("Command: " + sulDelegate.getCommand());
		}
		out.close();
		runDescription = sw.toString();
	}

	public String getRunDescription() {
		return runDescription;
	}

	public int getStates() {
		return states;
	}

	protected void setStates(int states) {
		this.states = states;
	}

	public long getLearnResets() {
		return learnResets;
	}

	protected void setLearnResets(long learnResets) {
		this.learnResets = learnResets;
	}

	public long getLearnInputs() {
		return learnInputs;
	}

	protected void setLearnInputs(long learnInputs) {
		this.learnInputs = learnInputs;
	}

	public long getAllResets() {
		return allResets;
	}

	protected void setAllResets(long allResets) {
		this.allResets = allResets;
	}

	public long getAllInputs() {
		return allInputs;
	}

	public void setAllInputs(long allInputs) {
		this.allInputs = allInputs;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	protected long getLastHypResets() {
		return lastHypResets;
	}

	protected void setLastHypResets(long lastHypResets) {
		this.lastHypResets = lastHypResets;
	}

	public long getLastHypInputs() {
		return lastHypInputs;
	}

	protected void setLastHypInputs(long lastHypInputs) {
		this.lastHypInputs = lastHypInputs;
	}

	protected void setCounterexamples(List<DefaultQuery<?, ?>> counterexamples) {
		this.counterexamples = counterexamples;
	}

	protected void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setHypStats(List<HypothesisStatistics> hypStats) {
		this.hypStats = hypStats;
	}
}
