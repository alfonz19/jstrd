package strd.lib;

import strd.lib.hid.HidLibrary;

public class StreamDeckOriginalV2 extends AbstractStreamDeck {

    public static final int KEY_COUNT = 15;
    public static final int ROW_COUNT = 3;
    public static final int COLUMN_COUNT = 5;

    public StreamDeckOriginalV2(HidLibrary.StreamDeckInfo streamDeckInfo,
                                HidLibrary.StreamDeckHandle streamDeckHandle) {
        super(streamDeckInfo, streamDeckHandle, KEY_COUNT, ROW_COUNT, COLUMN_COUNT);
    }
}
