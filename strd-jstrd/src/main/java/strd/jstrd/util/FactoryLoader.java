package strd.jstrd.util;

import strd.jstrd.configuration.StreamDeckConfiguration.ActionConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ButtonConfiguration;
import strd.jstrd.configuration.StreamDeckConfiguration.ContainerConfiguration;
import strd.jstrd.exception.InvalidConfigurationException;
import strd.jstrd.exception.JstrdException;
import strd.jstrd.streamdeck.unfinished.ConfigurableFactory;
import strd.jstrd.streamdeck.unfinished.action.ActionFactory;
import strd.jstrd.streamdeck.unfinished.button.ButtonFactory;
import strd.jstrd.streamdeck.unfinished.buttoncontainer.ButtonContainerFactory;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class FactoryLoader {
    //hide me!
    private FactoryLoader() {
    }

    public static ButtonContainerFactory findButtonContainerFactory(ContainerConfiguration containerConfigurationConfig) {
        return findButtonContainerFactory(containerConfigurationConfig.getType());
    }

    public static ButtonContainerFactory findButtonContainerFactory(String type) {
        return ServiceLoaderUtil.getService(ButtonContainerFactory.class, FactoryLoader.factoryHasType(type))
                .orElseThrow(unableToFindFactory("container", type));
    }

    public static ButtonFactory findButtonFactory(ButtonConfiguration buttonConfig) {
        return findButtonFactory(buttonConfig.getType());
    }

    public static ButtonFactory findButtonFactory(String type) {
        return ServiceLoaderUtil.getService(ButtonFactory.class, FactoryLoader.factoryHasType(type))
                .orElseThrow(unableToFindFactory("button", type));
    }

    public static ActionFactory findActionFactory(ActionConfiguration actionConfiguration) {
        return findActionFactory(actionConfiguration.getType());
    }

    public static ActionFactory findActionFactory(String type) {
        return ServiceLoaderUtil.getService(ActionFactory.class, FactoryLoader.factoryHasType(type))
                .orElseThrow(unableToFindFactory("action", type));
    }

    private static <T extends ConfigurableFactory> Predicate<T> factoryHasType(String type) {
        return buttonFactory -> buttonFactory.getObjectType().equals(type);
    }

    private static Supplier<JstrdException> unableToFindFactory(String whatFactory, String forType) {
        return () -> new InvalidConfigurationException(String.format("unable to find %s factory for type: \"%s\"",
                whatFactory,
                forType));
    }
}
