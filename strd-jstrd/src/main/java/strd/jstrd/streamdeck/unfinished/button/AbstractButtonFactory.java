package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.streamdeck.unfinished.AbstractConfigurableFactory;
import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractButtonFactory extends AbstractConfigurableFactory<Button> implements ButtonFactory {
    private final Function<Map<String, Object>, Button> ctorFunction;

    public AbstractButtonFactory(String objectType,
                                 Supplier<Button> ctorFunction,
                                 FactoryPropertiesDefinition configurationDefinition) {
        this(objectType, e -> ctorFunction.get(), configurationDefinition);
    }

    public AbstractButtonFactory(String objectType, Supplier<Button> ctorFunction) {
        this(objectType, e -> ctorFunction.get());
    }

    public AbstractButtonFactory(String objectType, Function<Map<String, Object>, Button> ctorFunction) {
        this(objectType, ctorFunction, FactoryPropertiesDefinition.EMPTY);
    }

    public AbstractButtonFactory(String objectType,
                                 Function<Map<String, Object>, Button> ctorFunction,
                                 FactoryPropertiesDefinition configurationDefinition) {
        super(objectType, configurationDefinition);
        this.ctorFunction = ctorFunction;
    }

    @Override
    public Button create(Map<String, Object> properties) {
        return ctorFunction.apply(properties);
    }
}
