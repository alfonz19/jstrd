package strd.jstrd.streamdeck.unfinished.button;

import strd.lib.iconpainter.IconPainter;

import java.time.Instant;

public class ColorButton implements Button {

    private final int red;
    private final int green;
    private final int blue;

    public ColorButton() {
        this(0, 0, 0);
    }

    public ColorButton(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void tick(Instant instant) {
        //do nothing
    }

    @Override
    public void draw() {
        throw new UnsupportedOperationException("Not implemented yet");//TODO MMUCHA: implement!!
    }

    @Override
    public void preload(IconPainter iconPainter) {
        byte[] bytes = iconPainter.fillWholeIcon(red, green, blue).toDeviceNativeFormat();
    }
}
