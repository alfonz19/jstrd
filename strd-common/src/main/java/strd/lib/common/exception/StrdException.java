package strd.lib.common.exception;

public class StrdException extends RuntimeException {

    public StrdException() {
        super();
    }

    public StrdException(Throwable e) {
        super(e);
    }

    public StrdException(String message) {
        super(message);
    }

    public StrdException(String message, Exception cause) {
        super(message, cause);
    }
}
