package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractButtonFactory implements ButtonFactory {

    private final String objectType;
    private final Function<Map<String, Object>, Button> ctorFunction;
    private final FactoryPropertiesDefinition configurationDefinition;



    public AbstractButtonFactory(String objectType,
                                 Supplier<Button> ctorFunction,
                                 FactoryPropertiesDefinition configurationDefinition) {
        this(objectType, e -> ctorFunction.get(), configurationDefinition);
    }

    public AbstractButtonFactory(String objectType,
                                 Supplier<Button> ctorFunction) {
        this(objectType, e -> ctorFunction.get());
    }

    public AbstractButtonFactory(String objectType,
                                 Function<Map<String, Object>, Button> ctorFunction) {
        this(objectType, ctorFunction, FactoryPropertiesDefinition.EMPTY);
    }

    public AbstractButtonFactory(String objectType,
                                 Function<Map<String, Object>, Button> ctorFunction,
                                 FactoryPropertiesDefinition configurationDefinition) {
        this.objectType = objectType;
        this.ctorFunction = ctorFunction;
        this.configurationDefinition = configurationDefinition;
    }

    @Override
    public String getObjectType() {
        return objectType;
    }

    @Override
    public Button create(Map<String, Object> properties) {
        return ctorFunction.apply(properties);
    }

    @Override
    public FactoryPropertiesDefinition getConfigurationDefinition() {
        return configurationDefinition;
    }
}
