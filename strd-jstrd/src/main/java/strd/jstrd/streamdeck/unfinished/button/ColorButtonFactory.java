package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ButtonConfiguration;
import strd.jstrd.streamdeck.unfinished.AbstractConfigurableFactory;
import strd.jstrd.util.PropertiesUtil;
import strd.lib.common.exception.CannotHappenException;

import java.awt.*;
import java.util.Map;

import static strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition.PropertyDataType.COLOR;
import static strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition.PropertyDefinition.exceptionMessageAsErrorString;

public class ColorButtonFactory extends AbstractConfigurableFactory implements ButtonFactory {

    public static final String COLOR_PROPERTY_NAME = "color";

    public ColorButtonFactory() {
        super("color");
        getConfigurationDefinition().addProperty(true,
                COLOR_PROPERTY_NAME,
                COLOR,
                PropertiesUtil.COLOR_PROPERTY_DESC,
                exceptionMessageAsErrorString((propertyDefinition, propertyValue) ->
                        PropertiesUtil.parseColorValue(propertyDefinition.getPropertyName(), propertyValue)));
    }

    @Override
    public Button create(ButtonConfiguration buttonConfiguration) {
        return new ColorButton(buttonConfiguration);
    }
}
