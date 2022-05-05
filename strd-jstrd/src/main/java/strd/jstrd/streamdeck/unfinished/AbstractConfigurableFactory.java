package strd.jstrd.streamdeck.unfinished;

public abstract class AbstractConfigurableFactory<T> implements ConfigurableFactory<T> {

    private final String objectType;
    private final FactoryPropertiesDefinition configurationDefinition;

    public AbstractConfigurableFactory(String objectType) {
        this(objectType, FactoryPropertiesDefinition.EMPTY);
    }

    public AbstractConfigurableFactory(String objectType,
                                       FactoryPropertiesDefinition configurationDefinition) {
        this.objectType = objectType;
        this.configurationDefinition = configurationDefinition;
    }

    @Override
    public String getObjectType() {
        return objectType;
    }

    @Override
    public FactoryPropertiesDefinition getConfigurationDefinition() {
        return configurationDefinition;
    }
}
