package strd.lib.common.exception;

public class CannotHappenException extends StrdException {

    private static final String MOCKING_MESSAGE = "Coding error. " +
            "Ironically, our developer thought this will never happen. Apologies for his poor judgement.";

    public CannotHappenException() {
        this((Exception) null);
    }

    public CannotHappenException(Exception cause) {
        super(MOCKING_MESSAGE, cause);
    }

    public CannotHappenException(String explanation) {
        super(String.format("%s His incorrect explanation why this cannot happen: \"%s\"",
                MOCKING_MESSAGE,
                explanation));
    }
}