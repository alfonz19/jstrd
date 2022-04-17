package strd.jstrd.streamdeck.unfinished.button;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractButtonFactory implements ButtonFactory {

    private final String buttonName;
    private final Function<Map<String, Object>, Button> ctorFunction;
    private final ButtonConfigurationDefinition configurationDefinition;



    public AbstractButtonFactory(String buttonName,
                                 Supplier<Button> ctorFunction,
                                 ButtonConfigurationDefinition configurationDefinition) {
        this(buttonName, e -> ctorFunction.get(), configurationDefinition);
    }

    public AbstractButtonFactory(String buttonName,
                                 Supplier<Button> ctorFunction) {
        this(buttonName, e -> ctorFunction.get());
    }

    public AbstractButtonFactory(String buttonName,
                                 Function<Map<String, Object>, Button> ctorFunction) {
        this(buttonName, ctorFunction, ButtonConfigurationDefinition.EMPTY);
    }

    public AbstractButtonFactory(String buttonName,
                                 Function<Map<String, Object>, Button> ctorFunction,
                                 ButtonConfigurationDefinition configurationDefinition) {
        this.buttonName = buttonName;
        this.ctorFunction = ctorFunction;
        this.configurationDefinition = configurationDefinition;
    }

    @Override
    public String getButtonName() {
        return buttonName;
    }

    @Override
    public Button create(Map<String, Object> properties) {
        return ctorFunction.apply(properties);
    }

    @Override
    public ButtonConfigurationDefinition getButtonConfigurationDefinition() {
        return configurationDefinition;
    }
}
