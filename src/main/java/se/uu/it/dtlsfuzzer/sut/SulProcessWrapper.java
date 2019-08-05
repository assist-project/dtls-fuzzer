package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import se.uu.it.dtlsfuzzer.config.SulDelegate;

/**
 * A SUL wrapper responsible for launching/terminating the process starting up
 * the SUL. Launches can be made at two distinct trigger points: (1) once at the
 * start, with termination taking place at the end of learning/testing; (2)
 * before executing each test, with termination done after the test has been
 * executed.
 */
public class SulProcessWrapper<I, O> implements SUL<I, O> {

	protected ProcessHandler handler = null;

	private SUL<I, O> sul;
	// TODO having the trigger here is not nice since it limits the trigger
	// options. Ideally we would have it outside.
	private ProcessLaunchTrigger trigger;

	public SulProcessWrapper(SUL<I, O> sul, SulDelegate sulDelegate) {
		this.sul = sul;
		if (handler == null)
			handler = new ProcessHandler(sulDelegate);
		this.trigger = sulDelegate.getProcessTrigger();
		if (trigger == ProcessLaunchTrigger.START && !handler.hasLaunched()) {
			handler.launchProcess();
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					handler.terminateProcess();
				}

			}));
		}
	}

	@Override
	public void pre() {
		if (trigger == ProcessLaunchTrigger.NEW_TEST) {
			handler.launchProcess();
		}
		sul.pre();
	}

	@Override
	public void post() {
		sul.post();
		if (trigger == ProcessLaunchTrigger.NEW_TEST) {
			handler.terminateProcess();
		}
	}

	@Override
	public O step(I in) throws SULException {
		return sul.step(in);
	}

	public boolean isAlive() {
		return handler.isAlive();
	}

}
