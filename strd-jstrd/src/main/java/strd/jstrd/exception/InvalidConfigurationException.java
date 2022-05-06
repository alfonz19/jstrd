package strd.jstrd.exception;

public class InvalidConfigurationException extends JstrdException {
    public InvalidConfigurationException(String message) {
        super("Incorrect configuration file: " + message);

    }
}
