package strd.jstrd.streamdeck.unfinished.buttoncontainer;

import strd.jstrd.streamdeck.unfinished.button.Button;
import strd.jstrd.streamdeck.unfinished.StreamDeckButtonSet;
import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Button container, which contains only buttons, no subgroups.
 */
public class SimpleButtonContainer implements ButtonContainer {
    private static final Logger log = getLogger(SimpleButtonContainer.class);

    private final List<Button> buttons;

    public SimpleButtonContainer(List<Button> buttons) {
        this.buttons = buttons;
    }

    @Override
    public void tick(Instant timestamp) {
        buttons.forEach(button -> button.tick(timestamp));
    }

    @Override
    public void update(StreamDeckButtonSet streamDeckButtonSet) {
        log.debug("Updating simple button container");
        IntStream.range(0, Math.min(streamDeckButtonSet.getButtonCount(), buttons.size()))
                .forEach(index -> streamDeckButtonSet.setIfAvailable(index, buttons::get));
    }

    @Override
    public void preload(Supplier<IconPainter> iconPainterSupplier) {
        buttons.forEach(e->e.preload(iconPainterSupplier));
    }
}
