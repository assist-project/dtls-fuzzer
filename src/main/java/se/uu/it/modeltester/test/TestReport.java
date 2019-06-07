package se.uu.it.modeltester.test;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestReport {
	private static final Logger LOG = LogManager.getLogger(TestReport.class);

	private List<Bug> reportedBugs;

	public TestReport() {
		reportedBugs = new LinkedList<>();
	}

	/**
	 * The important thing here the toString functionality which will be printed
	 * at the end
	 */
	public void addBug(Bug bug) {
		LOG.fatal(bug.toString());
		reportedBugs.add(bug);
	}

	public List<Bug> getBugs() {
		return Collections.unmodifiableList(reportedBugs);
	}

	public <T extends Bug> List<T> getBugs(Class<T> bugType) {
		return reportedBugs.stream().filter(b -> bugType.isInstance(b))
				.map(b -> bugType.cast(b)).collect(Collectors.toList());
	}

	public void printReport(PrintStream ps) {
		// PrintWriter pw = new PrintWriter(writer);
		ps.println("Testing Report");
		ps.println("");
		for (Bug bug : reportedBugs) {
			ps.println(bug);
		}
	}
}
