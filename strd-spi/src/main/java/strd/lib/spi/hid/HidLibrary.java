package strd.lib.spi.hid;

import java.time.Duration;
import java.util.List;

public interface HidLibrary {
    List<StreamDeckInfo> findStreamDeckDevices();
    StreamDeckHandle createStreamDeckHandle(StreamDeckInfo streamDeckInfo);

    void addListener(HidLibrary.DeviceListener listener);

    void removeListener(HidLibrary.DeviceListener listener);

    void removeListeners();

    void setPollingInterval(Duration duration);

    interface StreamDeckInfo {
        short getVendorId();
        short getProductId();
        StreamDeckVariant getStreamDeckVariant();
        String getSerialNumberString();
        String getProductString();
    }

    interface DeviceListener {
        void deviceAdded(StreamDeckInfo streamDeckInfo);

        void deviceRemoved(StreamDeckInfo streamDeckInfo);
    }
}
