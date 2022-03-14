package strd.lib;

import strd.lib.hid.HidLibrary;

import java.util.List;

public class StreamDeckFactory {
    private final HidLibrary hidLibrary;

    public StreamDeckFactory(HidLibrary hidLibrary) {
        this.hidLibrary = hidLibrary;
    }

    public List<HidLibrary.StreamDeckInfo> findStreamDeckDevices() {
        return hidLibrary.findStreamDeckDevices();
    }

    public StreamDeck openConnection(HidLibrary.StreamDeckInfo streamDeckInfo) {
        return streamDeckInfo.getStreamDeckVariant().create(streamDeckInfo, hidLibrary.openDevice(streamDeckInfo));
//        return new StreamDeck(streamDeckInfo, hidLibrary.openDevice(streamDeckInfo));
    }
}
