package se.uu.it.jsse.examples.dtlsclientserver;

public interface EventListener {
	default void notifyStart() {
	}
	default void notifyStop() {
	}
}
