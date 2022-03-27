package strd.lib.iconpainter.factory;

import strd.lib.spi.hid.StreamDeckVariant;
import strd.lib.iconpainter.IconPainter;
import strd.lib.streamdeck.StreamDeck;

import java.io.InputStream;

public interface IconPainterFactory {
    boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant);
    IconPainter create(StreamDeck streamDeck);
    IconPainter create(StreamDeck streamDeck, InputStream imageByteStream);
    IconPainter create(StreamDeck streamDeck, byte[] bytes);
}
