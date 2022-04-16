package strd.jstrd.streamdeck.unfinished.buttoncontainer;

import strd.jstrd.streamdeck.unfinished.StreamDeckButtonSet;
import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

/**
 * Button container, which contains other button containers.
 */
public class CompositeButtonContainer implements ButtonContainer {
    private final List<ButtonContainer> containerList;

    public CompositeButtonContainer(List<ButtonContainer> containerList) {
        this.containerList = containerList;
    }

    @Override
    public void tick(Instant timestamp) {
        containerList.forEach(e -> e.tick(timestamp));
    }

    @Override
    public void update(StreamDeckButtonSet streamDeckButtonSet) {
        containerList.forEach(e -> e.update(streamDeckButtonSet));
    }

    @Override
    public void preload(Supplier<IconPainter> iconPainterSupplier) {
        containerList.forEach(e->e.preload(iconPainterSupplier));
    }
}
