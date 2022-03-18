package strd.lib;

import strd.lib.hid.StreamDeckInfo;

public interface StreamDeck extends AutoCloseable {

    StreamDeckInfo getStreamDeckInfo();
    boolean isClosed();
    void close();
    int getKeyCount();
    void addButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);
    void removeButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);
    void removeAllButtonsStateUpdatedListeners();
    void setDeviceRemovalListener(DeviceRemovalListener deviceRemovalListener);

    //TODO MMUCHA: should not be here, as it's implementation specific.
    void setButtonImage(int buttonIndex, byte[] buttonImage);
    void resetDevice();
    void setBrightness(int percent);
    void screenOn();
    void screenOff();

    /**
     * Issue request to device to fetch its serial number. Probably pointless, as
     * {@link StreamDeckInfo#getSerialNumberString} seems to return the same string.
     * @return serial number of device
     */
    String getSerialNumber();


    interface ButtonStateListener {
        void buttonsStateUpdated(StreamDeckInfo streamDeckInfo, boolean[] buttonStates);
        void buttonStateUpdated(StreamDeckInfo streamDeckInfo, int buttonIndex, boolean buttonState);

        class Adapter implements ButtonStateListener {

            @Override
            public void buttonsStateUpdated(StreamDeckInfo streamDeckInfo, boolean[] buttonStates) {
                //no op
            }

            @Override
            public void buttonStateUpdated(StreamDeckInfo streamDeckInfo,
                                           int buttonIndex,
                                           boolean buttonState) {
                //no op
            }
        }
    }

    interface DeviceRemovalListener {
        void deviceRemoved(StreamDeckInfo streamDeckInfo);
    }
}
