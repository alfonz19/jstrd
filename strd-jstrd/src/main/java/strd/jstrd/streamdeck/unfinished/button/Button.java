package strd.jstrd.streamdeck.unfinished.button;

import strd.lib.iconpainter.IconPainter;

import java.time.Instant;

public interface Button {
//    void buttonPressed();
//
//    void buttonReleased();

    void tick(Instant instant);

    void draw();

    /**
     * Called during init phase, so that static stuff can be preload and cached for speedup.
     * @param iconPainter
     */
    void preload(IconPainter iconPainter);

    boolean needsUpdate();
}
