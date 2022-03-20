package strd.lib.streamdeck;

import strd.lib.StrdException;

import java.util.ServiceLoader;

public interface IconPainterFactory {

    //TODO MMUCHA: should not be part of lib.
    static IconPainterFactory findIconPainter(StreamDeck streamDeck) {
        return findIconPainter(streamDeck.getStreamDeckInfo().getStreamDeckVariant());
    }

    //TODO MMUCHA: should not be part of lib.
    static IconPainterFactory findIconPainter(StreamDeckVariant streamDeckVariant) {
        ServiceLoader<IconPainterFactory> factories = ServiceLoader.load(IconPainterFactory.class);
        return factories.stream()
                .map(ServiceLoader.Provider::get)
                .filter(e -> e.canProcessStreamDeckVariant(streamDeckVariant))
                .findFirst()
                .orElseThrow(() -> new StrdException("Cannot find IconPainter for stream deck variant " +
                        streamDeckVariant));
    }

    boolean canProcessStreamDeckVariant(StreamDeckVariant streamDeckVariant);

    IconPainter createEmpty();
    IconPainter create(int red, int green, int blue);
//    IconPainter create(int pixelCountPerIconSide, File file);
//    IconPainter create(int pixelCountPerIconSide, URI uri);

    interface IconPainter {
        IconPainter fillRect(int x1, int y1, int x2, int y2, int red, int green, int blue);
        IconPainter drawRect(int x1, int y1, int x2, int y2, int red, int green, int blue);
        byte[] toDeviceNativeFormat();
    }

}
