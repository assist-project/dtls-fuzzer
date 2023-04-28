package se.uu.it.dtlsfuzzer.sut;

public interface DynamicPortProvider {
    /**
     * Gets the local port of the (D)TLS SUL
     */
    public Integer getSULPort();
}
