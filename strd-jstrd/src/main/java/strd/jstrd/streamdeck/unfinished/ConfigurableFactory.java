package strd.jstrd.streamdeck.unfinished;

import java.util.List;
import java.util.Map;

public interface ConfigurableFactory {
    /**
     * @return name of what factory creates.
     */
    String getObjectType();

    FactoryPropertiesDefinition getConfigurationDefinition();

    List<String> validateProperties(Map<String, Object> properties);
}
