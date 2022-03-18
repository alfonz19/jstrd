package strd.lib;

import strd.lib.hid.StreamDeckInfo;

import java.util.List;

public interface StreamDeckManager {
    List<StreamDeckInfo> findStreamDeckDevices();
    StreamDeck openConnection(StreamDeckInfo streamDeckInfo);
}
