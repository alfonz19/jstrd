package strd.lib.streamdeck;

import strd.lib.hid.HidLibrary;
import strd.lib.hid.StreamDeckHandle;

import java.util.List;

public class StreamDeckManager {
    private final HidLibrary hidLibrary;

    public StreamDeckManager(HidLibrary hidLibrary) {
        this.hidLibrary = hidLibrary;
    }

    public List<HidLibrary.StreamDeckInfo> findStreamDeckDevices() {
        return hidLibrary.findStreamDeckDevices();
    }

    public StreamDeck openConnection(HidLibrary.StreamDeckInfo streamDeckInfo) {
        StreamDeckHandle streamDeckHandle = hidLibrary.createStreamDeckHandle(streamDeckInfo);
        return streamDeckInfo.getStreamDeckVariant().create(streamDeckHandle);
    }
}
