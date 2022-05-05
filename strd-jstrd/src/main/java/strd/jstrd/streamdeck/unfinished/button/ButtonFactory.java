package strd.jstrd.streamdeck.unfinished.button;

import strd.jstrd.streamdeck.unfinished.ConfigurableFactory;

import java.util.Map;

public interface ButtonFactory extends ConfigurableFactory<Button> {

    Button create(Map<String, Object> properties);

}
