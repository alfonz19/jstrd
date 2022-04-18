package strd.jstrd.streamdeck.unfinished.button;

import strd.lib.iconpainter.IconPainter;

import java.time.Instant;
import java.util.function.Supplier;

//TODO MMUCHA: missing dismiss call when button is removed. Should call close on iconpainter. Provide abstract parent instead which accepts iconpainter and release method?
public interface Button extends AutoCloseable {
//    void buttonPressed();
//
//    void buttonReleased();

    void tick(Instant instant);

    byte[] draw();

    //TODO MMUCHA: I want rather IconPainter here.
    /**
     * Called during init phase, so that static stuff can be preload and cached for speedup.
     * @param iconPainterSupplier
     */
    void preload(Supplier<IconPainter> iconPainterSupplier);

    /**
     * @return true, if internal state of this button was somehow changed, and if we would call draw now, the resultant
     * image would be different. False otherwise.
     *
     * Example: Button which shows clock, to minute precision only. If button is still visible, and we would attempt
     * to refresh it each second, there won't be any difference in resultant image. If we know, that there is
     * no difference, we should return false from this method. If there is no change, we may skip this button and speed
     * up processing.
     */
    boolean needsUpdate();

    /**
     * Terminate method. Button won't be used any more after this call. It should release all data which needs to be
     * released, including potentially created IconPainter, if needed.
     */
    void close();

    void updateButtonState(boolean buttonState);
}
