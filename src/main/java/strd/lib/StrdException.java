package strd.lib;

public class StrdException extends RuntimeException{
    public StrdException(Exception e) {
        super(e);
    }

    public StrdException(String message) {
        super(message);
    }
}
