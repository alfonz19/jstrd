package strd.lib.iconpainter.factory;


import strd.lib.common.exception.UnsupportedStreamDeckVariantException;
import strd.lib.iconpainter.BufferedImageIconPainterForStreamDeckMk2;
import strd.lib.iconpainter.IconPainter;
import strd.lib.spi.hid.StreamDeckVariant;
import strd.lib.streamdeck.StreamDeckDevice;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BufferedImageIconPainterFactory implements IconPainterFactory {

    private final Map<StreamDeckVariant, IconPainterFactory> supportedVariants;

    public BufferedImageIconPainterFactory() {
        supportedVariants = new HashMap<>();
        supportedVariants.put(StreamDeckVariant.STREAM_DECK_MK2, new Mk2());
    }

    @Override
    public boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant) {
        return supportedVariants.containsKey(streamDeckVariant);
    }

    @Override
    public IconPainter create(StreamDeckDevice streamDeck) {
        return getFactoryForVariant(streamDeck).create(streamDeck);
    }

    @Override
    public IconPainter create(StreamDeckDevice streamDeck, InputStream imageByteStream) {
        return getFactoryForVariant(streamDeck).create(streamDeck, imageByteStream);
    }

    @Override
    public IconPainter create(StreamDeckDevice streamDeck, byte[] bytes) {
        return getFactoryForVariant(streamDeck).create(streamDeck, bytes);
    }

    private IconPainterFactory getFactoryForVariant(StreamDeckDevice streamDeck) {
        StreamDeckVariant streamDeckVariant = streamDeck.getStreamDeckInfo().getStreamDeckVariant();
        IconPainterFactory factory = supportedVariants.get(streamDeckVariant);
        if (factory == null) {
            String message = String.format("This IconPainter does not support this variant of streamDeck: %s",
                    streamDeckVariant.toString());
            throw new UnsupportedStreamDeckVariantException(message);
        }
        return factory;
    }

    //----------------------------

    private static class Mk2 implements IconPainterFactory {

        @Override
        public boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant) {
            return streamDeckVariant == StreamDeckVariant.STREAM_DECK_MK2;
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
}
