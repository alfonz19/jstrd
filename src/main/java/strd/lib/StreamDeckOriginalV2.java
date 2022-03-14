package strd.lib;

import strd.lib.hid.HidLibrary;

public class StreamDeckOriginalV2 extends AbstractStreamDeck {
    public StreamDeckOriginalV2(HidLibrary.StreamDeckInfo streamDeckInfo,
                                HidLibrary.StreamDeckHandle streamDeckHandle) {
        super(streamDeckInfo, streamDeckHandle, 15, 3, 5);
    }
}
