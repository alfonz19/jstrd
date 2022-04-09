package strd.lib.iconpainter.factory;

import strd.lib.spi.hid.StreamDeckVariant;
import strd.lib.iconpainter.IconPainter;
import strd.lib.streamdeck.StreamDeckDevice;

import java.io.InputStream;

public interface IconPainterFactory {
    boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant);
    IconPainter create(StreamDeckDevice streamDeck);
    IconPainter create(StreamDeckDevice streamDeck, InputStream imageByteStream);
    IconPainter create(StreamDeckDevice streamDeck, byte[] bytes);
}
