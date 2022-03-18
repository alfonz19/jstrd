package strd.lib.hid;

import strd.lib.streamdeck.StreamDeckVariant;

import java.util.List;

public interface HidLibrary {
    List<StreamDeckInfo> findStreamDeckDevices();
    StreamDeckHandle createStreamDeckHandle(StreamDeckInfo streamDeckInfo);

    interface StreamDeckInfo {
        short getVendorId();
        short getProductId();
        StreamDeckVariant getStreamDeckVariant();
        String getSerialNumberString();
        String getProductString();
    }
}
