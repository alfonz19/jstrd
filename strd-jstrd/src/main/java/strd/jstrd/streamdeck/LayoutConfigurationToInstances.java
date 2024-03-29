package strd.jstrd.streamdeck;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ButtonConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ContainerConfiguration;
import strd.jstrd.exception.InvalidConfigurationException;
import strd.jstrd.streamdeck.unfinished.FactoryPropertiesDefinition;
import strd.jstrd.streamdeck.unfinished.button.Button;
import strd.jstrd.streamdeck.unfinished.button.ButtonFactory;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.ButtonContainer;
import strd.jstrd.util.FactoryLoader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LayoutConfigurationToInstances {

    private final ContainerConfiguration layout;

    public LayoutConfigurationToInstances(StreamDeckConfiguration.DeviceConfiguration configuration) {
        layout = configuration.getLayout();
    }


    public ButtonContainer transform() {
        return transform(layout);
    }

    private ButtonContainer transform(ContainerConfiguration containerConfig) {
        if (containerConfig.isLeafContainer()) {
            List<Button> buttonList = Optional.ofNullable(containerConfig.getButtons()).orElse(Collections.emptyList())
                    .stream()
                    .map(this::createButtonFromConfiguration)
                    .collect(Collectors.toList());

            return FactoryLoader.findButtonContainerFactory(containerConfig)
                    .createLeafContainer(containerConfig.getProperties(), buttonList);
        } else {
            List<ButtonContainer> buttonContainerList =
                    containerConfig.getContainers().stream().map(this::transform).collect(Collectors.toList());

            return FactoryLoader.findButtonContainerFactory(containerConfig)
                    .createContainer(containerConfig.getProperties(), buttonContainerList);

        }
    }

    private Button createButtonFromConfiguration(ButtonConfiguration buttonConfig) {
        ButtonFactory buttonFactory = FactoryLoader.findButtonFactory(buttonConfig);

        if (buttonConfig.getConditionalConfigurations().isEmpty() && hasRequiredProperty(buttonFactory)) {
            throw new InvalidConfigurationException(String.format("Button type=\"%s\", name=\"%s\" misses required configuration.", buttonConfig.getType(), buttonConfig.getName()));
        }

        //TODO MMUCHA: describe failures better, provide what fails and where(path to failure).
        //validate every configuration provided in button config.
        List<String> validationIssues = buttonConfig.getConditionalConfigurations()
                .stream()
                .map(StreamDeckConfiguration.ConditionalButtonConfiguration::getProperties)
                .flatMap(e -> buttonFactory.validateProperties(e).stream())
                .collect(Collectors.toList());

        if (!validationIssues.isEmpty()) {
            throw new InvalidConfigurationException("Following issues were found: " + validationIssues);
        }
        return buttonFactory.create(buttonConfig);
    }

    private boolean hasRequiredProperty(ButtonFactory buttonFactory) {
        return buttonFactory.getConfigurationDefinition()
                .getPropertyDefinitions()
                .stream()
                .anyMatch(FactoryPropertiesDefinition.PropertyDefinition::isRequired);
    }
}
