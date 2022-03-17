package strd.lib;

import strd.lib.hid.HidLibrary;

public interface StreamDeck extends AutoCloseable {

    HidLibrary.StreamDeckInfo getStreamDeckInfo();

    void addButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);

    void removeButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);

    void removeAllButtonsStateUpdatedListeners();

    void setDeviceRemovalListener(DeviceRemovalListener deviceRemovalListener);

    void setButtonImage(int buttonIndex, byte[] buttonImage);

    void resetDevice();

    void setBrightness(int percent);

    void screenOn();

    void screenOff();

    void getSerialNumber();




    boolean isClosed();
    void close();

    int getKeyCount();


    interface ButtonStateListener {
        void buttonsStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo, boolean[] buttonStates);
        void buttonStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo, int buttonIndex, boolean buttonState);

        class Adapter implements ButtonStateListener {

            @Override
            public void buttonsStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo, boolean[] buttonStates) {
                //no op
            }

            @Override
            public void buttonStateUpdated(HidLibrary.StreamDeckInfo streamDeckInfo,
                                           int buttonIndex,
                                           boolean buttonState) {
                //no op
            }
        }
    }

    interface DeviceRemovalListener {
        void deviceRemoved(HidLibrary.StreamDeckInfo streamDeckInfo);
    }
}