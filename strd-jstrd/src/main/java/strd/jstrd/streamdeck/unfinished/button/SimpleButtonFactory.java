package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.configuration.StreamDeckConfiguration.ButtonConfiguration;
import strd.jstrd.streamdeck.unfinished.AbstractConfigurableFactory;
import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;

import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleButtonFactory extends AbstractConfigurableFactory implements ButtonFactory {
    private final Function<ButtonConfiguration, Button> ctorFunction;

    public SimpleButtonFactory(String objectType,
                               Supplier<Button> ctorFunction,
                               FactoryPropertiesDefinition configurationDefinition) {
        this(objectType, e -> ctorFunction.get(), configurationDefinition);
    }

    public SimpleButtonFactory(String objectType, Supplier<Button> ctorFunction) {
        this(objectType, e -> ctorFunction.get());
    }

    public SimpleButtonFactory(String objectType, Function<ButtonConfiguration, Button> ctorFunction) {
        this(objectType, ctorFunction, FactoryPropertiesDefinition.EMPTY);
    }

    public SimpleButtonFactory(String objectType,
                               Function<ButtonConfiguration, Button> ctorFunction,
                               FactoryPropertiesDefinition configurationDefinition) {
        super(objectType, configurationDefinition);
        this.ctorFunction = ctorFunction;
    }

    @Override
    public Button create(ButtonConfiguration buttonConfiguration) {
        return ctorFunction.apply(buttonConfiguration);
    }
}
