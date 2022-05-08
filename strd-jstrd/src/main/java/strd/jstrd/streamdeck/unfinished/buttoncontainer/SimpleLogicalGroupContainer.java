package strd.jstrd.streamdeck.unfinished.buttoncontainer;

import strd.jstrd.streamdeck.unfinished.StreamDeckButtonSet;
import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class SimpleLogicalGroupContainer implements ButtonContainer {

    private static final Logger log = getLogger(SimpleLogicalGroupContainer.class);

    private final List<ButtonContainer> buttonContainers;

    public SimpleLogicalGroupContainer(List<ButtonContainer> buttonContainers) {
        this.buttonContainers = buttonContainers;
    }

    @SuppressWarnings("unused")
    public SimpleLogicalGroupContainer(Map<String, Object> properties, List<ButtonContainer> buttonContainers) {
        this(buttonContainers);
    }


    @Override
    public void tick(Instant timestamp) {
        buttonContainers.forEach(bc->bc.tick(timestamp));
    }

    @Override
    public void update(StreamDeckButtonSet streamDeckButtonSet) {
        buttonContainers
                .stream()
                //TODO MM: el
                .filter(e->true)
                .forEach(bc->bc.update(streamDeckButtonSet));
    }

    @Override
    public void preload(Supplier<IconPainter> iconPainterSupplier) {
        buttonContainers.forEach(bc->bc.preload(iconPainterSupplier));
    }
}
