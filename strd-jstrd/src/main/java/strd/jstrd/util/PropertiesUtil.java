package strd.jstrd.util;

import strd.jstrd.exception.JstrdException;

import java.awt.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesUtil {

    public static final String COLOR_PROPERTY_DESC =
            "Color of background. Example: '#FFFFFF'. No alpha, no nothing. Symbol # and 6 hex digits.";

    public static Color getColorProperty(Map<String, Object> properties, String propertyName) {
        Object colorValue = properties.get(propertyName);
        return parseColorValue(propertyName, colorValue);
    }

    public static Color parseColorValue(String propertyName, Object colorValue) {
        String colorString = getColorString(propertyName, colorValue);

        if (colorString == null) {
            throw createInvalidConfigurationException("null", propertyName);
        }

        Matcher matcher = Pattern.compile("#([a-fA-F0-9]{6})").matcher(colorString);
        if (!matcher.matches()) {
            throw createInvalidConfigurationException(colorString, propertyName);
        }

        try {
            return Color.decode("0x" + matcher.group(1));
        } catch (NumberFormatException e) {
            throw createInvalidConfigurationException(colorString, propertyName);
        }
    }

    private static String getColorString(String propertyName, Object colorValue) {
        try {
            return (String) colorValue;
        } catch (ClassCastException | NumberFormatException e) {
            throw createInvalidConfigurationException(colorValue, propertyName);
        }

    }

    private static JstrdException createInvalidConfigurationException(Object providedColorString,
                                                                      String colorPropertyName) {
        //TODO MMUCHA: custom exception
        return new JstrdException(String.format("Illegal value of property \"%s\"(%s). Provided value: \"%s\"",
                colorPropertyName, COLOR_PROPERTY_DESC, providedColorString));
    }
}
