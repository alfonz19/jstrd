package strd.lib.streamdeck;

import strd.lib.spi.hid.StreamDeckHandle;
import strd.lib.spi.hid.StreamDeckVariant;

import java.util.function.Function;

public class AbstractStreamDeckFactory implements StreamDeckFactory {
    private final StreamDeckVariant variant;
    private final Function<StreamDeckHandle, StreamDeck> ctor;

    public AbstractStreamDeckFactory(StreamDeckVariant variant, Function<StreamDeckHandle, StreamDeck> ctor) {
        this.variant = variant;
        this.ctor = ctor;
    }

    @Override
    public StreamDeckVariant creates() {
        return variant;
    }

    @Override
    public StreamDeck create(StreamDeckHandle streamDeckHandle) {
        return ctor.apply(streamDeckHandle);
    }

}
