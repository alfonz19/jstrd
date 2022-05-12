package strd.jstrd.streamdeck.unfinished.action;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.streamdeck.unfinished.ConfigurableFactory;

public interface ActionFactory extends ConfigurableFactory {

    Action create(StreamDeckConfiguration.ActionConfiguration actionConfiguration);


}
