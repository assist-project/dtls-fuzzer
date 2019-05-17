package se.uu.it.modeltester.execute;

import java.util.ArrayList;
import java.util.List;

public class ExecutionContext {
	
	private List<StepContext> stepContexes;
	
	public ExecutionContext() {
		stepContexes = new ArrayList<>();
	}
	
	public void addStepContext() {
		stepContexes.add(new StepContext());
	}
	
	
	public StepContext getStepContext() {
		if (!stepContexes.isEmpty())
			return stepContexes.get(stepContexes.size()-1);
		return null;
	}
	
	public StepContext getStepContext(int ind) {
		return stepContexes.get(ind);
	}
	
	public int getStepCount() {
		return stepContexes.size();
	}
	
}
