package strd.jstrd.streamdeck.unfinished.buttoncontainer;

import strd.jstrd.streamdeck.unfinished.StreamDeckButtonSet;
import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.util.function.Supplier;

public interface ButtonContainer {
    /**
     * clock tick. Just global update based on refresh interval. This should be propagated through all layout so that
     * all elements depending on time can update their internal state.
     */
    void tick(Instant timestamp);

    //TODO MMUCHA: ability to call update out of time interval based on internal action.
    void update(StreamDeckButtonSet streamDeckButtonSet);

    /**
     * Called during init phase, so that static stuff can be preload and cached for speedup.
     * @param iconPainterSupplier
     */
    void preload(Supplier<IconPainter> iconPainterSupplier);
}
