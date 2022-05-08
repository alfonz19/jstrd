package strd.jstrd.streamdeck.unfinished.buttoncontainer;

public class SimpleLogicalGroupContainerFactory extends AbstractNonLeafButtonContainerFactory {

    public SimpleLogicalGroupContainerFactory() {
        super("simpleLogicalGroupContainer", SimpleLogicalGroupContainer::new);
    }
}
