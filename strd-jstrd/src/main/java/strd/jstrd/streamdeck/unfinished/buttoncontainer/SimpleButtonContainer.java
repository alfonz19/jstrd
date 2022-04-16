package strd.jstrd.streamdeck.unfinished.buttoncontainer;

import strd.jstrd.streamdeck.unfinished.button.Button;
import strd.jstrd.streamdeck.unfinished.StreamDeckButtonSet;
import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.util.List;

/**
 * Button container, which contains only buttons, no subgroups.
 */
public class SimpleButtonContainer implements ButtonContainer {
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
        streamDeckButtonSet.setButtonsIfAvailable(buttons::get);
    }

    @Override
    public void preload(IconPainter iconPainter) {
        buttons.forEach(e->e.preload(iconPainter));
    }
}
