package strd.lib;

public interface StreamDeck extends AutoCloseable {

    public void addButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);

    void removeButtonsStateUpdatedListener(ButtonStateListener buttonsStateUpdatedListener);

    void removeAllButtonsStateUpdatedListeners();

    public void addDeviceRemovalListener() ;

    public void setButtonImage(int buttonIndex, byte[] buttonImage);

    public void resetDevice();

    public void setBrightness(int percent);

    public void screenOff();

    public void getSerialNumber();



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
