package de.rub.nds.modelfuzzer.fuzz;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestingReport {
	private static final Logger LOG = LogManager.getLogger(TestingReport.class);
	
	private List<Object> reportItems;
	
	public TestingReport() {
		reportItems = new LinkedList<>();
	}
	
	/**
	 * The important thing here the toString functionality which will be 
	 * printed at the end
	 */
	public void addItem(Object item) {
		LOG.fatal(item.toString());
		reportItems.add(item);
	}
	
	
	public void printReport(PrintStream ps) {
//		PrintWriter pw = new PrintWriter(writer);
		ps.println("Testing Report");
		for (Object item : reportItems) {
			ps.println(item);
		}
	}
}
