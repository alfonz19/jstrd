package strd.lib.iconpainter.factory;

import strd.lib.spi.hid.StreamDeckVariant;
import strd.lib.iconpainter.BufferedImageIconPainterForStreamDeckMk2;
import strd.lib.iconpainter.IconPainter;
import strd.lib.streamdeck.StreamDeckDevice;

import java.io.InputStream;

public class BufferedImageIconPainterFactoryForStreamDeckMk2 extends AbstractBufferedImageIconPainterFactory {

    public BufferedImageIconPainterFactoryForStreamDeckMk2() {
        super(StreamDeckVariant.STREAM_DECK_MK2);
    }

    @Override
    public IconPainter create(StreamDeckDevice streamDeck) {
        return new BufferedImageIconPainterForStreamDeckMk2(getIconSize(streamDeck));
    }

    @Override
    public IconPainter create(StreamDeckDevice streamDeck, InputStream imageByteStream) {
        return new BufferedImageIconPainterForStreamDeckMk2(getIconSize(streamDeck), imageByteStream);
    }

    @Override
    public IconPainter create(StreamDeckDevice streamDeck, byte[] bytes) {
        return new BufferedImageIconPainterForStreamDeckMk2(getIconSize(streamDeck), bytes);
    }

    private int getIconSize(StreamDeckDevice streamDeck) {
        return streamDeck.getStreamDeckInfo().getStreamDeckVariant().getPixelCountPerIconSide();
    }
}
