package strd.jstrd.streamdeck;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ButtonConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ContainerConfiguration;
import strd.jstrd.exception.InvalidConfigurationException;
import strd.jstrd.exception.JstrdException;
import strd.jstrd.streamdeck.unfinished.button.Button;
import strd.jstrd.streamdeck.unfinished.button.ButtonFactory;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.ButtonContainer;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.ButtonContainerFactory;
import strd.jstrd.util.ServiceLoaderUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

            return getButtonContainerFactory(containerConfig)
                    .createLeafContainer(containerConfig.getProperties(), buttonList);
        } else {
            List<ButtonContainer> buttonContainerList =
                    containerConfig.getContainers().stream().map(this::transform).collect(Collectors.toList());

            return getButtonContainerFactory(containerConfig)
                    .createContainer(containerConfig.getProperties(), buttonContainerList);

        }
    }

    private ButtonContainerFactory getButtonContainerFactory(ContainerConfiguration containerConfig) {
        Predicate<ButtonContainerFactory> containerFilteringPredicate =
                buttonContainerFactory -> buttonContainerFactory.getObjectType().equals(containerConfig.getType());

        Supplier<JstrdException> throwIfContainerNotFound = () -> new InvalidConfigurationException(String.format(
                "unable to find button container factory for type \"%s\"",
                containerConfig.getType()));

        return ServiceLoaderUtil.getService(ButtonContainerFactory.class, containerFilteringPredicate)
                .orElseThrow(throwIfContainerNotFound);
    }

    private Button createButtonFromConfiguration(ButtonConfiguration buttonConfig) {
        Predicate<ButtonFactory> buttonFilteringPredicate =
                buttonFactory -> buttonFactory.getObjectType().equals(buttonConfig.getType());

        Supplier<JstrdException> throwIfButtonNotFound =
                () -> new InvalidConfigurationException(String.format("unable to find button factory for type: \"%s\"",
                        buttonConfig.getType()));

        ButtonFactory buttonFactory = ServiceLoaderUtil.getService(ButtonFactory.class, buttonFilteringPredicate)
                .orElseThrow(throwIfButtonNotFound);

        return buttonFactory.create(buttonConfig.getProperties());
    }
}
