package strd.lib;

public class StdrException extends RuntimeException{
    public StdrException(Exception e) {
        super(e);
    }

    public StdrException(String message) {
        super(message);
    }
}
