package strd.lib.streamdeck;

import strd.lib.spi.hid.StreamDeckVariant;

public class StreamDeckOriginalV2Factory extends AbstractStreamDeckFactory {
    public StreamDeckOriginalV2Factory() {
        super(StreamDeckVariant.STREAM_DECK_MK2, StreamDeckOriginalV2::new);
    }
}
