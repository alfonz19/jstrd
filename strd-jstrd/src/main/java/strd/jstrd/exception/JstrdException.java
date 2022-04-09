package strd.jstrd.exception;

public class JstrdException extends RuntimeException {
    public JstrdException(Throwable cause) {
        super(cause);
    }

    public JstrdException(String message, Exception cause) {
        super(message, cause);
    }

    public JstrdException(String message) {
        super(message);

    }
}
