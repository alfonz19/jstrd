package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.exception.JstrdException;
import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;
import strd.lib.iconpainter.IconPainter;

import java.awt.*;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition.PropertyDataType.COLOR;

public class ColorButton implements Button {

    private final int red;
    private final int green;
    private final int blue;
    private byte[] iconBytes;

    public ColorButton() {
        this(0, 0, 0);
    }

    public ColorButton(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public ColorButton(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void tick(Instant instant) {
        //do nothing
    }

    @Override
    public byte[] draw() {
        return iconBytes;
    }

    @Override
    public void preload(Supplier<IconPainter> iconPainterSupplier) {
        try (IconPainter iconPainter = iconPainterSupplier.get()) {
            iconBytes = iconPainter.fillWholeIcon(red, green, blue).toDeviceNativeFormat();
        }
    }

    @Override
    public boolean needsUpdate() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("Color button r=%s, g=%d, b=%d", red, green, blue);
    }

    @Override
    public void close() {
        //do nothing.
    }

    public static class Factory implements ButtonFactory {

        public static final String COLOR_PROPERTY_NAME = "color";
        public static final String COLOR_PROPERTY_DESC =
                "Color of background. Example: '#FFFFFF'. No alpha, no nothing. Symbol # and 6 hex digits.";

        @Override
        public String getObjectType() {
            return "color";
        }

        @Override
        public Button create(Map<String, Object> properties) {
            return getColorProperty(properties.get(COLOR_PROPERTY_NAME));
        }

        private ColorButton getColorProperty(Object colorValue) {
            String colorString = getColorString(colorValue);

            Matcher matcher = Pattern.compile("#([a-fA-F0-9]{6})").matcher(colorString);
            if (! matcher.matches()) {
                throw createInvalidConfigurationException(colorString);
            }

            try {
                return new ColorButton(Color.decode("0x" + matcher.group(1)));
            } catch (NumberFormatException e) {
                throw createInvalidConfigurationException(colorString);
            }
        }

        private String getColorString(Object colorValue) {
            try {
                return (String) colorValue;
            } catch (ClassCastException|NumberFormatException e) {
                throw createInvalidConfigurationException(colorValue);
            }

        }

        private JstrdException createInvalidConfigurationException(Object providedColorString) {
            //TODO MMUCHA: custom exception
            return new JstrdException(String.format("Illegal value of property \"%s\"(%s). Provided value: \"%s\"",
                    COLOR_PROPERTY_NAME, COLOR_PROPERTY_DESC, providedColorString));
        }

        @Override
        public FactoryPropertiesDefinition getConfigurationDefinition() {
            return new FactoryPropertiesDefinition()
                    .addProperty(true,
                            COLOR_PROPERTY_NAME,
                            COLOR,
                            COLOR_PROPERTY_DESC,
                            FactoryPropertiesDefinition.PropertyDefinition.exceptionMessageAsErrorString(this::getColorProperty));
        }
    }
}
