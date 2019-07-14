package se.uu.it.dtlsfuzzer.sut;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import se.uu.it.dtlsfuzzer.config.SulDelegate;

/**
 * A SUL wrapper which joins the SUL with a handler for the process that
 * launches/terminates it.
 */
public class SulProcessWrapper<I, O> implements SUL<I, O> {

	private static boolean started = false;
	// TODO we are limited to one process because of this.
	protected static ProcessHandler handler = null;

	private SUL<I, O> sul;
	// TODO having the trigger here is not nice since it limits the trigger
	// options. Ideally we would have it outside.
	private ProcessLaunchTrigger trigger;

	public SulProcessWrapper(SUL<I, O> sul, SulDelegate sulDelegate) {
		this.sul = sul;
		if (handler == null)
			handler = new ProcessHandler(sulDelegate);
		this.trigger = sulDelegate.getProcessTrigger();
		if (trigger == ProcessLaunchTrigger.START && !started) {
			started = true;
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
