package strd.lib.streamdeck;

import java.awt.image.BufferedImage;

public class BufferedImageIconPainterFactoryForStreamDeckMk2 extends AbstractBufferedImageIconPainterFactory {

    public BufferedImageIconPainterFactoryForStreamDeckMk2() {
        super(StreamDeckVariant.STREAM_DECK_MK2.getPixelCountPerIconSide());
    }

    @Override
    public boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant) {
        return StreamDeckVariant.STREAM_DECK_MK2 == streamDeckVariant;
    }

    @Override
    protected byte[] toDeviceNativeFormatImpl(BufferedImage bi) {
        return writeJpgWithMaxQuality(flipHorizontallyAndVertically(bi));
    }
}
