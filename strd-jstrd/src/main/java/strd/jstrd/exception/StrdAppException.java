package strd.jstrd.exception;

public class StrdAppException extends RuntimeException {
    public StrdAppException(Throwable cause) {
        super(cause);
    }

    public StrdAppException(String message, Exception cause) {
        super(message, cause);
    }

    public StrdAppException(String message) {
        super(message);

    }
}
