package strd.lib.common.exception;

public class StrdException extends RuntimeException{
    public StrdException(Exception e) {
        super(e);
    }

    public StrdException(String message) {
        super(message);
    }

    public StrdException(String message, Exception cause) {
        super(message, cause);
    }
}
