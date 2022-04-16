package strd.jstrd.configuration;

import strd.jstrd.exception.InvalidSteamDeckConfigurationException;
import strd.jstrd.exception.JstrdException;
import strd.jstrd.util.JacksonUtil;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ConfigurationParser {

    public static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public StreamDeckConfiguration parse(File configurationFile) {
        return parse(readJsonFromFile(configurationFile));
    }

    private String readJsonFromFile(File configurationFile) {
        try {
            return createJsonStringFromBytes(new FileInputStream(configurationFile).readAllBytes());
        } catch (FileNotFoundException e) {
            throw new JstrdException(String.format("Configuration file %s not found", configurationFile));
        } catch (IOException e) {
            throw new JstrdException(String.format("Unable to read from configuration file %s", configurationFile));
        }
    }

    public StreamDeckConfiguration parse(InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        String json = readJsonFromInputStream(is);
        return parse(json);
    }

    private String readJsonFromInputStream(InputStream is) {
        try {
            byte[] bytes = is.readAllBytes();
            return createJsonStringFromBytes(bytes);
        } catch (IOException e) {
            throw new JstrdException("Unable to read configuration data", e);
        }
    }

    private String createJsonStringFromBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public StreamDeckConfiguration parse(String json) {
        StreamDeckConfiguration streamDeckConfiguration = JacksonUtil.deserializeJson(json);

        Set<ConstraintViolation<StreamDeckConfiguration>> validationResult =
                VALIDATOR.validate(streamDeckConfiguration);

        if (validationResult.isEmpty()) {
            return streamDeckConfiguration;
        } else {
            throw new InvalidSteamDeckConfigurationException(json, validationResult);
        }
    }

}
