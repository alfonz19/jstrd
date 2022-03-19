package strd.lib.streamdeck;

import strd.lib.hid.HidLibrary;
import strd.lib.hid.PureJavaHid;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface StreamDeck extends AutoCloseable {

    HidLibrary.StreamDeckInfo getStreamDeckInfo();
    boolean isClosed();
    void close();
    void addButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);
    void removeButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);
    void removeAllButtonsStateUpdatedListeners();
    void setDeviceRemovalListener(DeviceRemovalListener deviceRemovalListener);

    /**
     * Method to be used when setting up the configuration is some app, or by other one-off use.
     *
     * @param buttonIndex index of button to be set
     * @param buttonImage image data in device specific format.
     */
    void setButtonImage(int buttonIndex, byte[] buttonImage);

    /**
     * Method should be used for preparing the data, caching them, and using them with {@link #setButtonImage(byte[][])}
     * when needed to save some preparing time.
     *
     * @param buttonIndex index of button to be set
     * @param buttonImage image data in device specific format.
     * @param processSetImagePayload what to do with each packet of (potentially, probably) split image data.
     */
    void splitNativeImageBytesAndProcess(int buttonIndex, byte[] buttonImage, Consumer<byte[]> processSetImagePayload);

    /**
     * sends prepared button image data to device.
     * @param payloadsBytes
     */
    void setButtonImage(byte[][] payloadsBytes);


    void resetDevice();
    void setBrightness(int percent);
    void screenOn();
    void screenOff();

    /**
     * Issue request to device to fetch its serial number. Probably pointless, as
     * {@link PureJavaHid.StreamDeckInfoImpl#getSerialNumberString} seems to return the same string.
     * @return serial number of device
     */
    String getSerialNumber();


    interface ButtonStateListener {
        void buttonsStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo, boolean[] buttonStates);
        void buttonStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo, int buttonIndex, boolean buttonState);

        class Adapter implements ButtonStateListener {

            @Override
            public void buttonsStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo, boolean[] buttonStates) {
                //no op
            }

            @Override
            public void buttonStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo, int buttonIndex, boolean buttonState) {
                //no op
            }
        }
    }

    interface DeviceRemovalListener {
        void deviceRemoved(HidLibrary.StreamDeckInfo streamDeckInfo);
    }
}
