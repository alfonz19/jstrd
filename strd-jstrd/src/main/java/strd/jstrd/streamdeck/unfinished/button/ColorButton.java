package strd.jstrd.streamdeck.unfinished.button;

import strd.lib.iconpainter.IconPainter;

import java.awt.*;
import java.time.Instant;
import java.util.function.Supplier;

public class ColorButton implements Button {

    private final int red;
    private final int green;
    private final int blue;
    private byte[] iconBytes;

    public ColorButton() {
        this(0, 0, 0);
    }

    public ColorButton(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public ColorButton(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void tick(Instant instant) {
        //do nothing
    }

    @Override
    public byte[] draw() {
        return iconBytes;
    }

    @Override
    public void preload(Supplier<IconPainter> iconPainterSupplier) {
        try (IconPainter iconPainter = iconPainterSupplier.get()) {
            iconBytes = iconPainter.fillWholeIcon(red, green, blue).toDeviceNativeFormat();
        }
    }

    @Override
    public boolean needsUpdate() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("Color button r=%s, g=%d, b=%d", red, green, blue);
    }

    @Override
    public void close() {
        //do nothing.
    }

    @Override
    public void updateButtonState(boolean buttonState) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
