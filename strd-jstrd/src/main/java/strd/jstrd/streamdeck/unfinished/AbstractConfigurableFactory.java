package strd.jstrd.streamdeck.unfinished;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractConfigurableFactory implements ConfigurableFactory {

    private final String objectType;
    private final FactoryPropertiesDefinition configurationDefinition;

    public AbstractConfigurableFactory(String objectType) {
        this(objectType, new FactoryPropertiesDefinition());
    }

    public AbstractConfigurableFactory(String objectType, FactoryPropertiesDefinition configurationDefinition) {
        this.objectType = objectType;
        this.configurationDefinition = configurationDefinition;
    }

    @Override
    public final String getObjectType() {
        return objectType;
    }

    @Override
    public final FactoryPropertiesDefinition getConfigurationDefinition() {
        return configurationDefinition;
    }

    public List<String> validateProperties(Map<String, Object> properties) {
        Stream<String> invalidProperties = getConfigurationDefinition().getPropertyDefinitions()
                .stream()
                .map(e -> e.validateValue(properties.get(e.getPropertyName())))
                .filter(Optional::isPresent)
                .map(Optional::get);


        Set<String> knownProperties = getConfigurationDefinition().getPropertyDefinitions()
                .stream()
                .map(FactoryPropertiesDefinition.PropertyDefinition::getPropertyName)
                .collect(Collectors.toSet());

        Stream<String> unknownProperties = (properties == null ? Collections.<String>emptySet() : properties.keySet())
                .stream()
                .filter(e -> !knownProperties.contains(e))
                .map(e -> String.format("Unknown property \"%s\"", e));


        return Stream.concat(invalidProperties, unknownProperties).collect(Collectors.toList());
    }

}
