package strd.lib.streamdeck;

import strd.lib.spi.hid.StreamDeckHandle;
import strd.lib.spi.hid.StreamDeckVariant;

import java.util.function.Function;

public class AbstractStreamDeckFactory implements StreamDeckFactory {
    private final StreamDeckVariant variant;
    private final Function<StreamDeckHandle, StreamDeckDevice> ctor;

    public AbstractStreamDeckFactory(StreamDeckVariant variant, Function<StreamDeckHandle, StreamDeckDevice> ctor) {
        this.variant = variant;
        this.ctor = ctor;
    }

    @Override
    public StreamDeckVariant creates() {
        return variant;
    }

    @Override
    public StreamDeckDevice create(StreamDeckHandle streamDeckHandle) {
        return ctor.apply(streamDeckHandle);
    }

}
