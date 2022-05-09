package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ButtonConfiguration;
import strd.jstrd.exception.JstrdException;
import strd.jstrd.streamdeck.unfinished.AbstractConfigurableFactory;
import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;
import strd.lib.common.exception.CannotHappenException;

import java.awt.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition.PropertyDataType.COLOR;

public class ColorButtonFactory extends AbstractConfigurableFactory implements ButtonFactory {

    public static final String COLOR_PROPERTY_NAME = "color";
    public static final String COLOR_PROPERTY_DESC =
            "Color of background. Example: '#FFFFFF'. No alpha, no nothing. Symbol # and 6 hex digits.";

    public ColorButtonFactory() {
        super("color", new FactoryPropertiesDefinition()
                .addProperty(true,
                        COLOR_PROPERTY_NAME,
                        COLOR,
                        COLOR_PROPERTY_DESC,
                        FactoryPropertiesDefinition.PropertyDefinition.exceptionMessageAsErrorString(ColorButtonFactory::getColorProperty)));
    }

    @Override
    public Button create(ButtonConfiguration buttonConfiguration) {
        Map<String, Object> props =
                buttonConfiguration.findApplicableConditionalButtonConfiguration()
                        .map(StreamDeckConfiguration.ConditionalButtonConfiguration::getProperties)
                        .orElseThrow(() -> new CannotHappenException("Should be protected by validation"));
        return getColorProperty(props.get(COLOR_PROPERTY_NAME));
    }

    private static ColorButton getColorProperty(Object colorValue) {
        String colorString = getColorString(colorValue);

        if (colorString == null) {
            throw createInvalidConfigurationException("null");
        }

        Matcher matcher = Pattern.compile("#([a-fA-F0-9]{6})").matcher(colorString);
        if (!matcher.matches()) {
            throw createInvalidConfigurationException(colorString);
        }

        try {
            return new ColorButton(Color.decode("0x" + matcher.group(1)));
        } catch (NumberFormatException e) {
            throw createInvalidConfigurationException(colorString);
        }
    }

    private static String getColorString(Object colorValue) {
        try {
            return (String) colorValue;
        } catch (ClassCastException | NumberFormatException e) {
            throw createInvalidConfigurationException(colorValue);
        }

    }

    private static JstrdException createInvalidConfigurationException(Object providedColorString) {
        //TODO MMUCHA: custom exception
        return new JstrdException(String.format("Illegal value of property \"%s\"(%s). Provided value: \"%s\"",
                COLOR_PROPERTY_NAME, COLOR_PROPERTY_DESC, providedColorString));
    }
}
