package strd.jstrd.streamdeck.unfinished.action;

import strd.jstrd.configuration.StreamDeckConfiguration.ActionConfiguration;
import strd.jstrd.streamdeck.unfinished.AbstractConfigurableFactory;
import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;

import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleActionFactory extends AbstractConfigurableFactory implements ActionFactory {
    private final Function<ActionConfiguration, Action> ctorFunction;

    public SimpleActionFactory(String objectType,
                               Supplier<Action> ctorFunction,
                               FactoryPropertiesDefinition configurationDefinition) {
        this(objectType, e -> ctorFunction.get(), configurationDefinition);
    }

    public SimpleActionFactory(String objectType, Supplier<Action> ctorFunction) {
        this(objectType, e -> ctorFunction.get());
    }

    public SimpleActionFactory(String objectType, Function<ActionConfiguration, Action> ctorFunction) {
        this(objectType, ctorFunction, FactoryPropertiesDefinition.EMPTY);
    }

    public SimpleActionFactory(String objectType,
                               Function<ActionConfiguration, Action> ctorFunction,
                               FactoryPropertiesDefinition configurationDefinition) {
        super(objectType, configurationDefinition);
        this.ctorFunction = ctorFunction;
    }

    @Override
    public Action create(ActionConfiguration ActionConfiguration) {
        return ctorFunction.apply(ActionConfiguration);
    }
}
