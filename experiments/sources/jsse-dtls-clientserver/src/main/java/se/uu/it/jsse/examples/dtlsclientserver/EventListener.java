package se.uu.it.jsse.examples.dtlsclientserver;

public interface EventListener {

	void notifyStart();
	void notifyStop();

	public static EventListener getNopEventListener() {
		return NopEventListener.getInstance();
	}

	static class NopEventListener implements EventListener {
		private static NopEventListener INSTANCE;
		public static EventListener getInstance() {
			if (INSTANCE == null) {
				INSTANCE = new NopEventListener();
			}
			return INSTANCE;
		}

		private NopEventListener() {}

		@Override
		public void notifyStart() {
		}

		@Override
		public void notifyStop() {
		}
	}
}
