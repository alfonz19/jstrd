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

    List<String> validateProperties(Map<String, Object> properties);
}
