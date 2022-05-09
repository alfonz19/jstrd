package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.configuration.StreamDeckConfiguration;
import strd.jstrd.streamdeck.unfinished.ConfigurableFactory;

public interface ButtonFactory extends ConfigurableFactory {

    Button create(StreamDeckConfiguration.ButtonConfiguration buttonConfiguration);

}
