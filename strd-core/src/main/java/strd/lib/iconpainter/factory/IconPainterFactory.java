package strd.lib.iconpainter.factory;

import strd.lib.iconpainter.IconPainter;
import strd.lib.spi.hid.StreamDeckVariant;
import strd.lib.streamdeck.StreamDeckDevice;

import java.io.InputStream;

//TODO MMUCHA: factory must support all variants, as only 1 instance can run which can select only single painter.
public interface IconPainterFactory {
    boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant);
    IconPainter create(StreamDeckDevice streamDeck);
    IconPainter create(StreamDeckDevice streamDeck, InputStream imageByteStream);
    IconPainter create(StreamDeckDevice streamDeck, byte[] bytes);
}
