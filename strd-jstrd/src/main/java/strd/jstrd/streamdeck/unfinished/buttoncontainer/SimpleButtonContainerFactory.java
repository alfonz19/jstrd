package strd.jstrd.streamdeck.unfinished.buttoncontainer;

public class SimpleButtonContainerFactory extends AbstractLeafButtonContainerFactory {

    public SimpleButtonContainerFactory() {
        super("simpleButtonContainer", SimpleButtonContainer::new);
    }
}
