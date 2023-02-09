package se.uu.it.dtlsfuzzer.mapper;

public class ReceiveMessageException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 173897462143448940L;

    public ReceiveMessageException() {
        super();
    }

    public ReceiveMessageException(String message) {
        super(message);
    }

    public ReceiveMessageException(Throwable cause) {
        super(cause);
    }

    public ReceiveMessageException(String message, Throwable cause) {
        super(message, cause);
    }

}
