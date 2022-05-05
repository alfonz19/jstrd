package strd.jstrd.streamdeck.unfinished.buttoncontainer;

import strd.jstrd.streamdeck.unfinished.AbstractConfigurableFactory;
import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;
import strd.jstrd.streamdeck.unfinished.button.Button;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class AbstractLeafButtonContainerFactory extends AbstractConfigurableFactory<ButtonContainer> implements ButtonContainerFactory {
    private final BiFunction<Map<String, Object>, List<Button>, ButtonContainer> ctorFunction;

//    public AbstractButtonContainerFactory(String objectType,
//                                 Supplier<ButtonContainer> ctorFunction,
//                                 FactoryPropertiesDefinition configurationDefinition) {
//        this(objectType, e -> ctorFunction.get(), configurationDefinition);
//    }
//
//    public AbstractButtonContainerFactory(String objectType, Supplier<Button> ctorFunction) {
//        this(objectType, e -> ctorFunction.get());
//    }
//
    public AbstractLeafButtonContainerFactory(String objectType,
                                              BiFunction<Map<String, Object>, List<Button>, ButtonContainer> ctorFunction) {
        this(objectType, ctorFunction, FactoryPropertiesDefinition.EMPTY);
    }

    public AbstractLeafButtonContainerFactory(String objectType,
                                              BiFunction<Map<String, Object>, List<Button>, ButtonContainer> ctorFunction,
                                              FactoryPropertiesDefinition configurationDefinition) {
        super(objectType, configurationDefinition);
        this.ctorFunction = ctorFunction;
    }

    @Override
    public ButtonContainer createLeafContainer(Map<String, Object> properties, List<Button> children) {
        return ctorFunction.apply(properties, children);
    }

    @Override
    public ButtonContainer createContainer(Map<String, Object> properties, List<ButtonContainer> children) {
        //TODO MMUCHA: better exception and info.
        throw new IllegalStateException("This is not non-leaf container");
    }
}
