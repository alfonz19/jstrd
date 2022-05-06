package strd.jstrd.streamdeck.unfinished;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ConfigurableFactory {
    /**
     * @return name of what factory creates.
     */
    String getObjectType();

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
