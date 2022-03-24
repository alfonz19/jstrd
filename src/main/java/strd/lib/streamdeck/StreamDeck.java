package strd.lib.streamdeck;

import strd.lib.hid.HidLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
    default void setButtonImage(int buttonIndex, byte[] buttonImage) {
        setButtonImage(splitNativeImageBytes(buttonIndex, buttonImage));
    }

    /**
     * Method should be used for preparing the data, caching them, and using them with {@link #setButtonImage(List)}
     * when needed to save some preparing time.
     *
     * @param buttonIndex index of button to be set
     * @param buttonImage image data in device specific format.
     * @param processSetImagePayload what to do with each packet of (potentially, probably) split image data.
     */
    void splitNativeImageBytesAndProcess(int buttonIndex, byte[] buttonImage, BiConsumer<byte[], Integer> processSetImagePayload);

    default List<byte[]> splitNativeImageBytes(int buttonIndex, byte[] buttonImage) {
        List<byte[]> result = new ArrayList<>(10);
        splitNativeImageBytesAndProcess(buttonIndex, buttonImage, (bytes, length)-> result.add(bytes));
        return result;
    }

    /**
     * sends prepared button image data to device.
     * @param payloadsBytes list of image byte arrays, payloads to be sent.
     */
    void setButtonImage(List<byte[]> payloadsBytes);


    void resetDevice();
    void setBrightness(int percent);
    void screenOn();
    void screenOff();


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
