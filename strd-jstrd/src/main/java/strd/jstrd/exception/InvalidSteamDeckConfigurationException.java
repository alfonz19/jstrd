package strd.jstrd.exception;

import strd.jstrd.configuration.StreamDeckConfiguration;

import javax.validation.ConstraintViolation;
import java.util.Set;

public final class InvalidSteamDeckConfigurationException extends JstrdException {
    private final String json;
    private final Set<ConstraintViolation<StreamDeckConfiguration>> validationResult;

    public InvalidSteamDeckConfigurationException(String json, Set<ConstraintViolation<StreamDeckConfiguration>> validationResult) {
        super("Invalid streamdeck configuration");
        this.json = json;
        this.validationResult = validationResult;
    }

    public String getJson() {
        return json;
    }

    public Set<ConstraintViolation<StreamDeckConfiguration>> getValidationResult() {
        return validationResult;
    }
}
