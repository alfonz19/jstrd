package strd.lib;

//TODO MMUCHA: rename to strd...
public class StdrException extends RuntimeException{
    public StdrException(Exception e) {
        super(e);
    }

    public StdrException(String message) {
        super(message);
    }
}
