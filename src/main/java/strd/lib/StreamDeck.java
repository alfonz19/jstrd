package strd.lib;

public interface StreamDeck extends AutoCloseable {

    void addButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);

    void removeButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);

    void removeAllButtonsStateUpdatedListeners();

    void addDeviceRemovalListener() ;

    void setButtonImage(int buttonIndex, byte[] buttonImage);

    void resetDevice();

    void setBrightness(int percent);

    void screenOff();

    void getSerialNumber();



    //TODO MMUCHA: implement.
//    public final void screenOn() {
//
//    }

    //throttling!

    void close();

    int getKeyCount();

    interface ButtonStateListener {
        void buttonsStateUpdated(boolean[] buttonStates);
        void buttonStateUpdated(int buttonIndex, boolean buttonState);

        class Adapter implements ButtonStateListener {

            @Override
            public void buttonsStateUpdated(boolean[] buttonStates) {
                //no op
            }

            @Override
            public void buttonStateUpdated(int buttonIndex, boolean buttonState) {
                //no op
            }
        }
    }
}
