package se.uu.it.dtlsfuzzer.components.sul.core;

public class TlsSulAdapterException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TlsSulAdapterException(String message, Throwable cause) {
        super(message, cause);
    }

    public TlsSulAdapterException(String message) {
        super(message);
    }

    public TlsSulAdapterException(Throwable cause) {
        super(cause);
    }
}
