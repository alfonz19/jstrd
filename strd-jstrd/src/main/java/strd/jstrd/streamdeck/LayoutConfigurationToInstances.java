package strd.jstrd.streamdeck;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ButtonConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ContainerConfiguration;
import strd.jstrd.exception.JstrdException;
import strd.jstrd.streamdeck.unfinished.button.Button;
import strd.jstrd.streamdeck.unfinished.button.ButtonFactory;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.ButtonContainer;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.ButtonContainerFactory;
import strd.jstrd.util.ServiceLoaderUtil;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LayoutConfigurationToInstances {

    private final ContainerConfiguration layout;

    public LayoutConfigurationToInstances(StreamDeckConfiguration.DeviceConfiguration configuration) {
        layout = configuration.getLayout();
    }


    public ButtonContainer transform() {
        ContainerConfiguration container = layout;
        if (container.isLeafContainer()) {
            List<Button> buttonList = container.getButtons()
                    .stream()
                    .map(this::createButtonFromConfiguration)
                    .collect(Collectors.toList());

            return createContainerFromConfiguration(container, buttonList);
        } else {
            recursion
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    private ButtonContainer createContainerFromConfiguration(ContainerConfiguration containerConfig, List<Button> buttonList) {
        Predicate<ButtonContainerFactory> containerFilteringPredicate =
                buttonContainerFactory -> buttonContainerFactory.getObjectType().equals(containerConfig.getType());

        Supplier<JstrdException> throwIfContainerNotFound =
                () -> new JstrdException("Unable to find button container factory for type: " + containerConfig.getType());

        ButtonContainerFactory buttonContainerFactory =
                ServiceLoaderUtil.getLibrary(ButtonContainerFactory.class, containerFilteringPredicate)
                        .orElseThrow(throwIfContainerNotFound);

        return buttonContainerFactory.createLeafContainer(containerConfig.getProperties(), buttonList);
    }

    private Button createButtonFromConfiguration(ButtonConfiguration buttonConfig) {
        Predicate<ButtonFactory> buttonFilteringPredicate =
                buttonFactory -> buttonFactory.getObjectType().equals(buttonConfig.getType());

        Supplier<JstrdException> throwIfButtonNotFound =
                () -> new JstrdException("Unable to find button factory for type: " + buttonConfig.getType());

        ButtonFactory buttonFactory = ServiceLoaderUtil.getLibrary(ButtonFactory.class, buttonFilteringPredicate)
                .orElseThrow(throwIfButtonNotFound);

        return buttonFactory.create(buttonConfig.getProperties());
    }
}
