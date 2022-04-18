package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ConfigurableFactory<CREATES> {
    /**
     * @return name of what factory creates.
     */
    String getObjectType();

    CREATES create(Map<String, Object> properties);

    FactoryPropertiesDefinition getConfigurationDefinition();

    default Optional<String> validateProperties(Map<String, Object> properties) {
        String errorMessage = getConfigurationDefinition().getPropertyDefinitions()
                .stream()
                .map(e -> e.validateValue(properties.get(e.getPropertyName())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining(","));
        return errorMessage.isEmpty() ? Optional.empty() : Optional.of(errorMessage);
    }
}
