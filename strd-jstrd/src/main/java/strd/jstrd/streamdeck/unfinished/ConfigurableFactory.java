package strd.jstrd.streamdeck.unfinished;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ConfigurableFactory {
    /**
     * @return name of what factory creates.
     */
    String getObjectType();

    FactoryPropertiesDefinition getConfigurationDefinition();

    default List<String> validateProperties(Map<String, Object> properties) {
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
