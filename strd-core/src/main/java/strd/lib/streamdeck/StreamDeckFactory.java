package strd.lib.streamdeck;

import strd.lib.spi.hid.StreamDeckHandle;
import strd.lib.spi.hid.StreamDeckVariant;

public interface StreamDeckFactory {
    StreamDeckVariant creates();
    StreamDeck create(StreamDeckHandle streamDeckHandle);

}
