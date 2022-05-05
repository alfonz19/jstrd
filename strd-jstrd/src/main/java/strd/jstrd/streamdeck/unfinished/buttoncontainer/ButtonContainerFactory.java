package strd.jstrd.streamdeck.unfinished.buttoncontainer;

import strd.jstrd.streamdeck.unfinished.ConfigurableFactory;
import strd.jstrd.streamdeck.unfinished.button.Button;

import java.util.List;
import java.util.Map;

public interface ButtonContainerFactory extends ConfigurableFactory<ButtonContainer>  {

    ButtonContainer createLeafContainer(Map<String, Object> properties, List<Button> children);
    ButtonContainer createContainer(Map<String, Object> properties, List<ButtonContainer> children);

}
